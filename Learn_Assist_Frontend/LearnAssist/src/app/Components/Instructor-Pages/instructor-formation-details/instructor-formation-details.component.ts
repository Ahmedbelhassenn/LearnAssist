import { NgFor, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { SafeUrlPipe } from '../../../safe-url.pipe';
import { FormationService } from '../../../services/formation/formation.service';
import { Formation } from '../../../models/formation.model';
import { ServiceService } from '../../../services/service.service';
import { Router } from '@angular/router';
import { CourseService } from '../../../services/course/course.service';
import { LucideAngularModule,Eye, BookOpen, Users, Video, FileText, BarChart3, Plus, Bell, Settings, Trash2, Edit, CheckCircle, Upload, FileEdit } from 'lucide-angular';

import { FilesService } from '../../../services/files/files.service';
import { AddCoursesComponent } from '../../formation-Pages/add-courses/add-courses.component';
import { EditCourseComponent } from '../../formation-Pages/edit-course/edit-course.component';


interface Course{
  id: string,
  title: string,
}
@Component({
  selector: 'app-instructor-formation-details',
  standalone: true,
  imports: [NgFor, SafeUrlPipe, LucideAngularModule, NgIf, AddCoursesComponent, EditCourseComponent],
  templateUrl: './instructor-formation-details.component.html',
  styleUrl: './instructor-formation-details.component.css'
})

export class InstructorFormationDetailsComponent {

    icons={BookOpen, Users, Video, FileText, BarChart3, Plus, Bell, Settings, Trash2, Edit, CheckCircle, Eye, Upload, FileEdit}
    courses: Course[]=[];
    instructorProfilePhoto='profile.jpg'
    formation: Formation= {
      formationDuration: '',
      formationLevel: '',
      price: '',
      title: '',
      videoUrl: '',
      description: '',
      videoFileName:'',
      contents: [],
      imageUrl: '',
      formationStatus: '',
      instructorName: '',
      instructorProfilePhoto: '',
      instructorBio: ''
    };
    course: Course[]=[]
    idCourse= '';
    isEditFormationModalOpen= false;
    showEditCourseModal= false;
  
    constructor(private formationService: FormationService, private serviceService: ServiceService, 
      private courseService: CourseService, private router: Router, private fileService: FilesService) {}
  
    ngOnInit(): void {
      this.getFormation();
      this.getCourses();
    }
    getFormation(){
      const id = localStorage.getItem('idFormation') || '';
      this.formationService.getFormationById(id).subscribe({
        next: (response)=>{
          this.formation=response;
          this.formation.title=this.serviceService.capitalizeFirstLetter(this.formation.title)
          if(this.formation.instructorProfilePhoto!=null){
            this.serviceService.getProfilePicture(this.formation.instructorProfilePhoto).subscribe(
              (blob) => {
                const objectURL = URL.createObjectURL(blob);
                this.instructorProfilePhoto = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement de l'image", error);
              }
            );
          }

          if (this.formation.videoFileName) {
            this.fileService.getChapterVideo(this.formation.videoFileName).subscribe(
              (blob) => {
                const objectURL = URL.createObjectURL(blob);
                this.formation.videoUrl = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement du vidéo", error);
              }
            );
          }
          
        },
        error: (error)=>{
          console.log("error", error)
        }
      })
    }

    getCourses(): void{
      const id=localStorage.getItem('idFormation') || '';
      this.courseService.getCourses(id).subscribe({
        next: (response)=>{
          this.course=response.map((course: Course) => {
            course.title=this.serviceService.capitalizeFirstLetter(course.title);
            return course;
          })
          console.log("Courses uploaded successfully")
        },
        error: (error)=>{
          console.log("error", error)
        }
      })
    }

    goToCourseDetails(courseId: string) {
      localStorage.setItem('courseId', courseId);
      this.router.navigate(['/instructor/course-details', courseId]);
    }
    showDeleteModal = false;

    openDeleteModal(id: string): void {
      this.idCourse = id;
      this.showDeleteModal = true;
    }
    showAddCourseModal=false
    openAddCourseModal():void {
      this.showAddCourseModal=true
    }
    closeAddCourseModal(): void{
      this.showAddCourseModal=false;
      this.getCourses();
    }
    openEditCourseModal(id: string):void {
      localStorage.setItem("courseId", id);
      this.showEditCourseModal=true
      this.getCourses()
    }
    closeEditCourseModal(): void{
      this.showEditCourseModal=false;
      this.getCourses();
    }
    
    deleteCourse(): void {
      this.courseService.deleteCourse(this.idCourse).subscribe({
        next: () => {
          console.log("Cours supprimé");
          this.showDeleteModal = false; 
          this.getCourses()// rafraîchir la liste
        },
        error: (error) => {
          console.error("Erreur lors de la suppression :", error);
        }
      });
    }

    openEditFormationModal(id: string) {
      localStorage.setItem("idFormation",id);
      this.isEditFormationModalOpen=true;
    }
      
      closeEditFormationModal() {
        this.isEditFormationModalOpen = false;
      }
  
}
