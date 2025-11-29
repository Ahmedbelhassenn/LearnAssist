import { Component, Input, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { FormationService } from '../../../services/formation/formation.service';
import { ServiceService } from '../../../services/service.service';
import { CourseService } from '../../../services/course/course.service';

@Component({
  selector: 'app-add-courses',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf],
  templateUrl: './add-courses.component.html',
  styleUrl: './add-courses.component.css'
})
export class AddCoursesComponent implements OnInit {
  myformation : any= {
    title: '',
    description: '',
    imageUrl: '',
    price: '',
    formationDuration: '',
    formationLevel: '',
    videoUrl: '',
    emailInstructor:'',
    formationStatus:'draft',
    imageFileName:''
  };


  newCourse: any = {
    title: '',
    description: '',
    chapters: []
  };
  isCourseTitleExists = false;
  isCourseAdded = false;

  constructor(private formationService: FormationService, private courseService: CourseService){}
  ngOnInit(): void {
      this.getFormation();
  }

  getFormation(){
    const id = localStorage.getItem('idFormation') || '';
    this.formationService.getFormationById(id).subscribe({
      next: (response)=>{
        this.myformation=response;
      },
      error: (error)=>{
        console.log("error", error)
      }
    })
  }
  // Ajouter un chapitre vide
  addChapter() {
    this.newCourse.chapters.push({
      title: '',
      description: '',
      video: null,
      document: null,
      documentUrl: ''
    });
  }

  // Supprimer un chapitre par index
  removeChapter(index: number) {
    this.newCourse.chapters.splice(index, 1);
  }

  

  // Gérer les fichiers sélectionnés pour un chapitre spécifique
  onChapterFileSelected(event: any, chapterIndex: number, type: 'video' | 'document') {
    const file = event.target.files[0];
    if (file) {
      this.newCourse.chapters[chapterIndex][type] = file;
    }
  }

  // Soumettre le formulaire pour ajouter un cours
  addCourse() {
    const id= localStorage.getItem('idFormation') || null;
    const formData = new FormData();
    // Infos de base du cours
    formData.append('title', this.newCourse.title);
    formData.append('description', this.newCourse.description);
    // Ajout des chapitres (sous forme JSON pour texte + fichiers séparés)
    const chaptersData = this.newCourse.chapters.map((chapter: any, index: number) => {
      const chap = {
        title: chapter.title,
        description: chapter.description,
        quiz: chapter.documentUrl,
      };
      if (chapter.video) formData.append(`chapterVideo${index}`, chapter.video);
      if (chapter.document) formData.append(`chapterDocument${index}`, chapter.document);
      return chap;
    });
    formData.append('chapters', JSON.stringify(chaptersData));
    // 👇 Ici tu envoies `formData` vers ton service backend
    console.log('Cours à envoyer :', formData);
    console.log('cours:  ', this.newCourse);
    if(id!=null){
      this.courseService.addCourseToFormation(id, formData).subscribe({
        next: (response)=>{
          console.log('Course added successfully');
          this.isCourseAdded=true;
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
}
