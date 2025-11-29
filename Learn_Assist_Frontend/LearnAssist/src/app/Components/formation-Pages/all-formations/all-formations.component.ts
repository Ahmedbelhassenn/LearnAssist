import { NgClass, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { BarChart3, Bell, BookOpen, CheckCircle, Edit, Eye, FileEdit, FileText, LucideAngularModule, Plus, Search, Settings, Trash2, Upload, Users, Video } from 'lucide-angular';
import { FormationService } from '../../../services/formation/formation.service';
import { FilesService } from '../../../services/files/files.service';
import { FormsModule } from '@angular/forms';
import { AddCoursesComponent } from "../add-courses/add-courses.component";
import { EditFormationComponent } from "../edit-formation/edit-formation.component";
import { AddFormationComponent } from "../add-formation/add-formation.component";

interface Course {
  id: string;
  title: string;
  formationDuration: string;
  instructorName: string;
  formationLevel: string;
  price: string;
  imageUrl: string;
  imageFileName: string;
  status: string;
  description?: string;
  videoUrl?: string;
}

@Component({
  selector: 'app-all-formations',
  standalone: true,
  imports: [NgClass, NgIf, NgFor, LucideAngularModule, FormsModule, AddCoursesComponent, EditFormationComponent, AddFormationComponent],
  templateUrl: './all-formations.component.html',
  styleUrls: ['./all-formations.component.css']
})
export class AllFormationsComponent implements OnInit {
  // Icônes
  icons = {
    BookOpen, Users, Video, FileText, Search, Plus, Bell, 
    Settings, Trash2, Edit, CheckCircle, Eye, Upload, FileEdit
  };

  // Données 
  courses: Course[] = [];
  filteredCourses: Course[] = [];
  currentPage = 1;
  itemsPerPage = 6;
  pages: number[] = [];
  searchTerm = '';
  statusFilter = 'all';
  showAddCourseModal=false;
  idFormation='';
  isEditFormationModalOpen=false;
  isFormationModalOpen = false;
  isFormationsEmpty=false;

  constructor(
    private formationService: FormationService,
    private fileService: FilesService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadCourses();
  }

  loadCourses(): void {
    
    this.formationService.getInstructorCourses().subscribe({
      next: (response) => {
        this.courses = response.map((course: Course) => {
          this.isFormationsEmpty=false;
          this.fileService.getFormationImage(course.imageFileName).subscribe(
            (blob) => {
              const objectURL = URL.createObjectURL(blob);
              course.imageUrl = objectURL;
            },
            (error) => {
              console.error("Erreur lors du chargement de l'image", error);
            }
          );
          return course;
        });
        this.applyFilters();
      },
      error: (error) => {
        if(error.error==='No formation found for instructor'){
          this.isFormationsEmpty=true;
        }
        else{
          console.log('Erreur lors du chargement des cours', error);
        }
        
      }
    });
  }

  

  openAddCourseModal(id: string):void {
    localStorage.setItem("idFormation",id)
    this.showAddCourseModal=true
  }

  closeAddCourseModal(): void{
    this.showAddCourseModal=false;
  }
  showDeleteModal = false;

  openDeleteModal(id: string): void {
    this.idFormation = id;
    this.showDeleteModal = true;
  }
  
  deleteFormation(): void {
    this.formationService.deleteFormation(this.idFormation).subscribe({
      next: () => {
        console.log("Formation supprimée");
        this.courses = this.courses.filter(course => course.id !== this.idFormation);
        this.applyFilters(); // réappliquer les filtres sur les nouvelles données
        this.showDeleteModal = false;
        this.idFormation = ''; // nettoyer l'id
      },
      error: (error) => {
        console.error("Erreur lors de la suppression :", error);
      }
    });
  }
  
  
  
  showStatusModal = false;
  pendingStatus: 'published' | 'draft' = 'published';
  selectedCourse: any = null;
  
  openStatusModal(id: string, newStatus: 'published' | 'draft') {
    debugger;
    this.idFormation = id;
    this.pendingStatus = newStatus;
    this.showStatusModal = true;
  }
  
  cancelStatusChange() {
    this.showStatusModal = false;
    this.selectedCourse = null;
  }
  
  confirmStatusChange() {
      this.formationService.updateFormationStatus(this.idFormation,this.pendingStatus).subscribe({
        next: () => {
          console.log("état de formation changé");
          this.loadCourses(); // rafraîchir la liste
        },
        error: (error) => {
          console.error("Erreur lors de la suppression :", error);
        }
      });
    this.cancelStatusChange();
  }

  applyFilters(): void {
    // Filtre par recherche
    this.filteredCourses = this.courses.filter(course => 
      course.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
      course.description?.toLowerCase().includes(this.searchTerm.toLowerCase())
    );

    // Filtre par statut
    if (this.statusFilter !== 'all') {
      this.filteredCourses = this.filteredCourses.filter(
        course => course.status === this.statusFilter
      );
    }


  }

  resetFilters(): void {
    this.searchTerm = '';
    this.statusFilter = 'all';
    this.applyFilters();
  }

  goToFormationDetails(id: string) {
    localStorage.setItem('idFormation',id);
    this.router.navigateByUrl('/instructor/formation-details');
  }
  


  changeStatus(courseId: string, newStatus: string): void {
    this.formationService.updateFormationStatus(courseId, newStatus).subscribe({
      next: () => {
        const course = this.courses.find(c => c.id === courseId);
        if (course) {
          course.status = newStatus;
          this.applyFilters();
        }
      },
      error: (error) => {
        console.error('Erreur lors du changement de statut', error);
      }
    });
  }

  openFormationModal() {
    this.isFormationModalOpen = true;
  }

  closeFormationModal() {
    this.isFormationModalOpen = false;
    this.loadCourses(); // rafraîchir la liste
  }

  openEditFormationModal(id: string) {
    localStorage.setItem("idFormation",id);
    this.isEditFormationModalOpen=true;
  }
    
    closeEditFormationModal() {
      this.isEditFormationModalOpen = false;
      this.loadCourses(); // rafraîchir la liste
    }

  deleteCourse(courseId: string): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette formation ?')) {
      this.formationService.deleteFormation(courseId).subscribe({
        next: () => {
          this.courses = this.courses.filter(course => course.id !== courseId);
          this.loadCourses();
          this.applyFilters();
        },
        error: (error) => {
          console.error('Erreur lors de la suppression', error);
        }
      });
    }
  }

  ngOnDestroy(): void {
    // Nettoyage des URLs d'objets
    this.courses.forEach(course => {
      if (course.imageUrl && course.imageUrl.startsWith('blob:')) {
        URL.revokeObjectURL(course.imageUrl);
      }
    });
  }
}