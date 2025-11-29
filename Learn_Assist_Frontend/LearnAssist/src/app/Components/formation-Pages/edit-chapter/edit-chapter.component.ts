import { CommonModule, formatDate, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { FormationService } from '../../../services/formation/formation.service';
import { ServiceService } from '../../../services/service.service';
import { ChapterService } from '../../../services/chapter/chapter.service';

@Component({
  selector: 'app-edit-chapter',
  standalone: true,
  imports: [FormsModule, NgIf, CommonModule],
  templateUrl: './edit-chapter.component.html',
  styleUrl: './edit-chapter.component.css'
})
export class EditChapterComponent {


chapter: any={
    title:'',
    description: '',
    documentUrl:'',
    videoFileName: '',
    documentFileName: ''
  };


  constructor(private formationService: FormationService, private serviceService: ServiceService, 
      private chapterService: ChapterService, private router: Router) {}


  ngOnInit(): void {
    this.getChapters();
  }

  onChapterFileSelected(event: any, type: 'video' | 'document') {
    const file = event.target.files[0];
    if (file) {
      if (type === "video") {
        this.chapter.video = file;
      } else {
        this.chapter.document = file;
      }
    }
  }

  isChapterTitleExists=false;
  isChapterEdited= false;

  getChapters(): void{
    const id=localStorage.getItem('chapterId') || '';
    this.chapterService.getChapterById(id).subscribe({
      next: (response)=>{
        debugger
        this.chapter.title=response.chapter.title;
        this.chapter.description=response.chapter.description;
        this.chapter.documentUrl=response.chapter.quiz;
        this.chapter.videoFileName=response.chapter.videoFileName;
        this.chapter.documentFileName=response.chapter.documentFileName;
        debugger
      },
      error: (error)=>{
        console.log("error", error)
      }
    })
  }
  editChapter() {
    const id = localStorage.getItem("chapterId") || ''; 
    const formData = new FormData();
    formData.append("title", this.chapter.title);
    formData.append("description", this.chapter.description);
    formData.append("quiz", this.chapter.documentUrl);
    if(this.chapter.video){
      debugger
      formData.append("video", this.chapter.video);
    } 
    debugger
    if(this.chapter.document){
      debugger
      formData.append("document", this.chapter.document);
    } 
    this.chapterService.editChapter(id, formData ).subscribe({
      next: (response)=>{
        console.log("Ok")
        this.isChapterEdited=true
      },
      error: (error)=>{
        if (error.error.message==='Chapter with this title already exists'){
            this.isChapterTitleExists=true;
        }
        console.log("error", error)
      }
    })
  }
}
