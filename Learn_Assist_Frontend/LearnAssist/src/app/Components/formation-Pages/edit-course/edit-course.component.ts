import { NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CourseService } from '../../../services/course/course.service';
import { FormationService } from '../../../services/formation/formation.service';
import { ServiceService } from '../../../services/service.service';



@Component({
  selector: 'app-edit-course',
  standalone: true,
  imports: [FormsModule, NgIf],
  templateUrl: './edit-course.component.html',
  styleUrl: './edit-course.component.css'
})


export class EditCourseComponent  implements OnInit {
  course: any={
    title:'',
    description: ''
  };

  constructor(private formationService: FormationService, private serviceService: ServiceService, 
      private courseService: CourseService, private router: Router) {}

  ngOnInit(): void {
          this.getCourses()
      }
  isCourseTitleExists=false;
  isCourseEdited= false;

  getCourses(): void{
    const id=localStorage.getItem('courseId') || '';
    this.courseService.getCourseById(id).subscribe({
      next: (response)=>{
        this.course.title=response.course.title;
        this.course.description=response.course.description;
      },
      error: (error)=>{
        console.log("error", error)
      }
    })
  }
  editCourse() {
    const id = localStorage.getItem("courseId") || ''; 
    this.courseService.editCourse(id, this.course ).subscribe({
      next: (response)=>{
        console.log("Ok")
        this.isCourseEdited=true
      },
      error: (error)=>{
        if (error.error.message==='Course with this title already exists'){
            this.isCourseTitleExists=true;
        }
        console.log("error", error)
      }
    })
  }

}
