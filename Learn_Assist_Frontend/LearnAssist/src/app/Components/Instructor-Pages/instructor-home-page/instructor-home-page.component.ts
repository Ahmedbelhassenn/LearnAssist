import { Component, inject, OnInit } from '@angular/core';
import { ServiceService } from '../../../services/service.service';
import { FormsModule } from '@angular/forms';
import { CommonModule, NgClass, NgFor, NgIf } from '@angular/common';
import { Instructor } from '../../../models/instructor.model';
import { LucideAngularModule,Eye, BookOpen, Users, Video, FileText, BarChart3, Plus, Bell, Settings, Trash2, Edit, CheckCircle, Upload, FileEdit, X } from 'lucide-angular';
import { FormationService } from '../../../services/formation/formation.service';
import { InstructorService } from '../../../services/instructor/instructor.service';
import { AddFormationComponent } from "../../formation-Pages/add-formation/add-formation.component";
import { EditFormationComponent } from "../../formation-Pages/edit-formation/edit-formation.component";
import { Router, RouterLink } from '@angular/router';
import { AddCoursesComponent } from "../../formation-Pages/add-courses/add-courses.component";
import { FilesService } from '../../../services/files/files.service';
import { ChapterService } from '../../../services/chapter/chapter.service';
import { InscriptionFormationService } from '../../../services/inscriptionFormation/inscription-formation.service';
import { Article } from '../../../models/article.model';
import { AddArticleComponent } from "../../formation-Pages/add-article/add-article.component";
import { ArticleService } from '../../../services/article/article.service';


interface Stat {
  label: string;
  value: number;
  icon: any;
}

interface Course{
  formationDuration: string,
  instructorName: string,
  formationLevel: string,
  price: string,
  imageUrl: string,
  imageFileName: string,
  id: string,
  title: string,
  status: string
}

interface InscriptionRequest {
  id: string;
  participantName: string;
  participantEmail: string;
  formationTitle: string;
  createdAt: string;
  status: string
}




@Component({
  selector: 'app-instructor-home-page',
  standalone: true,
  imports: [FormsModule, NgFor, LucideAngularModule, NgIf, AddFormationComponent,
    NgClass, EditFormationComponent, AddCoursesComponent, RouterLink, CommonModule, AddArticleComponent],
  templateUrl: './instructor-home-page.component.html',
  styleUrl: './instructor-home-page.component.css'
})
export class InstructorHomePageComponent implements OnInit {
  // Données
  isFormationsEmpty= false;
  isPublished=true;
  isBioModalOpen: any;
  username = 'UserName';
  activeTotalFormation= 0;
  id='';
  idFormation=''
  profilePhotoUrl= '';
  isBiosaved= false;
  isEditFormationModalOpen=false;
  showDeleteModal = false;
  showAddCourseModal=false;
  showStatusModal = false;
  pendingStatus: 'published' | 'draft' = 'published';
  selectedCourse: any = null;
  displayedCourses: any[] = []; // Les 2 premières
  showViewMoreButton: boolean = false; // Pour afficher ou cacher "Voir plus"
  bio = ''; // Variable pour la bio
  isFormationModalOpen = false; // Variable pour contrôler l'ouverture du modal
  icons={BookOpen, Users, Video, FileText, BarChart3, Plus, Bell, Settings, 
  Trash2, Edit, CheckCircle, Eye, Upload, FileEdit, X}
  
  courses: Course[]=[];

  pendingRequests: InscriptionRequest[] = [];
  
  instructor: Instructor= {
       firstName: "",
       lastName: "",
       phone: "",
       email: "",
       gender: "",
       dateOfBirth: undefined , 
       city: "",
       speciality: "",
       password: "",
       confirmPassword: ''
  }
 
  constructor(private serviceService:ServiceService, private formationService: FormationService,private fileService: FilesService,
    private instructorService: InstructorService, private router: Router, private chapterService: ChapterService, 
    private inscriptionFormationService: InscriptionFormationService){}
  
  private articleService = inject(ArticleService);
    
    
  ngOnInit() {
    this.getInformation();
    this.getInstructorCourses();
    this.getTotalParticipants();
    this.getFormationsTotalActive();
    this.getInstructorTotalVideos();
    this.getInstructorTotalResources();
    this.loadPendingRequests();
    this.loadInstructorArticles();
  }

  toggleStatus(course: any) {
    course.status = course.status === 'Publié' ? 'Brouillon' : 'Publié';
  }

  getInformation(): void {
    this.instructorService.getInstructorInformation().subscribe({
      next: (response)=>{
        this.username=`${response.firstName} ${response.lastName}`;  
        this.profilePhotoUrl=response.profilePhotoUrl;
        if(response.bio==null){
          this.bio=this.username+' est un '
        }else{
          this.bio=response.bio;
        }
      },
      error: (error)=>{
        console.log(error.error)
      }
    })
  }
  
  openDeleteModal(id: string): void {
    this.idFormation = id;
    this.showDeleteModal = true;
  }
    
  openAddCourseModal(id: string):void {
    localStorage.setItem("idFormation",id)
    this.showAddCourseModal=true
  }
  closeAddCourseModal(): void{
    this.showAddCourseModal=false;
  }

  deleteFormation(): void {
    this.formationService.deleteFormation(this.idFormation).subscribe({
      next: () => {
        console.log("Formation supprimée");
        this.showDeleteModal = false;
        this.getInstructorCourses(); // rafraîchir la liste
      },
      error: (error) => {
        console.error("Erreur lors de la suppression :", error);
      }
    });
  }

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
          this.getInstructorCourses(); // rafraîchir la liste
        },
        error: (error) => {
          console.error("Erreur lors de la suppression :", error);
        }
      });
    this.cancelStatusChange();
  }

  openBioModal() {
    this.isBioModalOpen = true;
  }

  closeBioModal() {
    this.isBioModalOpen = false;
  }

  openFormationModal() {
    this.isFormationModalOpen = true;
  }

  closeFormationModal() {
    this.isFormationModalOpen = false;
    this.getInstructorCourses(); // rafraîchir la liste
  }

  openEditFormationModal(id: string) {
    localStorage.setItem("idFormation",id);
    this.isEditFormationModalOpen=true;
  }
    
  closeEditFormationModal() {
    this.isEditFormationModalOpen = false;
    this.getInstructorCourses(); // rafraîchir la liste
  }
    
  saveBio() {
    this.instructor.bio=this.bio;
    this.instructorService.ModifyInstructorBio(this.instructor).subscribe({
      next:(response)=>{
        this.isBiosaved=true;
      },
      error:(error)=>{
        console.log(error);
        alert("Il y a une erreur, merci de réessayer");
      }
    })
    this.closeBioModal();
  }

  getInstructorCourses() {
    this.formationService.getInstructorCourses().subscribe({
      next: (response) => {
        this.courses = response.map((course: Course) => {
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
        this.displayedCourses = this.courses.slice(0, 2); // On prend que les 2 premières
        this.showViewMoreButton = this.courses.length > 2; // Afficher "Voir plus" si plus de 2
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

  goToFormationDetails(id: string) {
    localStorage.setItem('idFormation',id);
    this.router.navigateByUrl('/instructor/formation-details');
  }
  
  stats: Stat[] = [];

  getFormationsTotalActive(): void{
    this.formationService.getFormationsTotalActive().subscribe({
      next : (response)=>{
        this.stats.push({ label: 'Cours actifs', value: response.result, icon: this.icons.BookOpen })
      },
      error : (error)=>{
        console.log("Il y a un problème lors de récupération de nombre de cours actives")
      }
    })
  }

  getInstructorTotalVideos(): void{
    this.chapterService.getInstructorTotalVideos().subscribe({
      next : (response)=>{
        this.stats.push({ label: 'Vidéos créées', value: response.result, icon: this.icons.Video })
      },
      error : (error)=> {
        console.log("Il y a un problème lors de récupération de nombre de vidéos")
        
      }
    })
  }

  getInstructorTotalResources(): void{
    this.chapterService.getInstructorTotalResources().subscribe({
      next : (response)=>{
        this.stats.push({ label: 'Resources', value: response.result, icon: this.icons.FileText })
      },
      error : (error)=> {
        console.log("Il y a un problème lors de récupération de nombre de ressources")
        
      }
    })
  }

  
  loadPendingRequests(): void {
    this.inscriptionFormationService.getAllInscriptionFormationByInstructor().subscribe(
      (response)=>{
        response.list.forEach((element: InscriptionRequest) => {
          if (element.status==="Pending"){
            this.pendingRequests.push(element)
          }
          
        });
      }
    )
  }

  confirmationModal = {
  isOpen: false,
  type: '', // 'approve' ou 'reject'
  data: null as any, // la demande sélectionnée
  };

  // Ouvrir le modal
  openConfirmationModal(request: any, type: 'approve' | 'reject') {
    this.confirmationModal = {
      isOpen: true,
      type,
      data: request
    };
  }

  // Fermer le modal sans action
  cancelConfirmation() {
    this.confirmationModal.isOpen = false;
  }
  onApprove(id: string) {
    
    this.inscriptionFormationService.onApprove(id).subscribe({
      next : (response)=>{
        console.log("approved successfully");
      },
      error: (error)=>{
        console.log("error:", error)
      }
    })
  }
  onReject(id: string) {
    this.inscriptionFormationService.onReject(id).subscribe({
      next :(response)=>{
        console.log("rejected successfully");
      },
      error: (error)=>{
        console.log("error:", error)
      }
    })
  }


  // Confirmer l'action
  confirmAction() {
    const requestId = this.confirmationModal.data.id;
    debugger

    if (this.confirmationModal.type === 'approve') {
      this.onApprove(requestId);
    } else {
      this.onReject(requestId);
    }

    this.confirmationModal.isOpen = false;
    setTimeout( ()=> {
      this.loadPendingRequests();
    }, 500
    )
    
  }

  getTotalParticipants(){
      this.inscriptionFormationService.getTotalParticipants().subscribe({
        next: (response)=>{
          this.stats.push({ label: 'Étudiants total', value: response.total, icon: this.icons.Users },)
        }
      })
  }
  // Propriétés pour la gestion des articles
  articles: Article[] = [];
  isArticleModalOpen = false;
  articleForm = {
    title: '',
    content: '',
    imageUrl: ''
  };

  // Méthodes pour gérer les articles
  openCreateArticleModal() {
    this.articleForm = { title: '', content: '', imageUrl: '' };
    this.isArticleModalOpen = true;
  }

  

  closeArticleModal() {
    this.isArticleModalOpen = false;
  }

  loadInstructorArticles() {
    this.articleService.getInstructorArticles().subscribe({
      next: (response) => {
        this.articles = response.map((article: Article) => {
          if (article.imageFileName) {
           this.fileService.getArticleImage(article.imageFileName).subscribe(
            (blob) => {
                const objectURL = URL.createObjectURL(blob);
                article.imageblob = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement de l'image", error);
              }
           ) 
          }
          return article;
        });
      },
      error: (error) => {
        console.error('Error loading articles:', error);
      }
    }); 
  }

  
}
 

