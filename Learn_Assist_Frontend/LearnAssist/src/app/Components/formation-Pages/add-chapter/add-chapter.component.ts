import { Component } from '@angular/core';
import { FormationService } from '../../../services/formation/formation.service';
import {  CommonModule, NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChapterService } from '../../../services/chapter/chapter.service';

@Component({
  selector: 'app-add-chapter',
  standalone: true,
  imports: [FormsModule, NgIf, CommonModule],
  templateUrl: './add-chapter.component.html',
  styleUrl: './add-chapter.component.css'
})
export class AddChapterComponent {
  isChapterTitleExists = false;
  isLoading = false; // Pour gérer l'état de chargement

  constructor(
    private formationService: FormationService, 
    private chapterService: ChapterService
  ) {}

  newChapter = {
    title: '',
    description: '',
    documentUrl: '',
    video: null as File | null,
    document: null as File | null
  };
  
  isChapterAdded = false;
  
  onChapterFileSelected(event: any, type: 'video' | 'document') {
    const file = event.target.files[0];
    if (file) {
      if (type === "video") {
        this.newChapter.video = file;
      } else {
        this.newChapter.document = file;
      }
    }
  }

  resetForm() {
    this.newChapter = {
      title: '',
      description: '',
      documentUrl: '',
      video: null,
      document: null
    };
    this.isChapterAdded = false;
    this.isChapterTitleExists = false;
    this.isLoading = false;
  }

  addChapter() {
    this.isLoading = true;
    this.isChapterTitleExists = false;

    const courseId = localStorage.getItem('courseId');
    if (!courseId) {
      console.error('Course ID not found');
      this.isLoading = false;
      return;
    }

    const formData = new FormData();
    formData.append('title', this.newChapter.title);
    formData.append('description', this.newChapter.description);
    if (this.newChapter.video) formData.append('video', this.newChapter.video);
    if (this.newChapter.document) formData.append('document', this.newChapter.document);
    formData.append('quiz', this.newChapter.documentUrl);

    this.chapterService.addChapterToCourse(courseId, formData).subscribe({
      next: (response) => {
        console.log('Chapter added successfully', response);
        this.isChapterAdded = true;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Error adding chapter', error);
        this.isLoading = false;
        
        if (error.error?.message === 'Chapter with this title already exists') {
          this.isChapterTitleExists = true;
        }
      }
    });
  }

}
