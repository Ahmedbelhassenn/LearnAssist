import {  NgIf, SlicePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Article } from '../../../models/article.model';
import { ArticleService } from '../../../services/article/article.service';

@Component({
  selector: 'app-add-article',
  standalone: true,
  imports: [FormsModule, NgIf, SlicePipe],
  templateUrl: './add-article.component.html',
  styleUrl: './add-article.component.css'
})
export class AddArticleComponent {
  articleForm: Article={title:'', content:'', publishedAt: new Date()};
  isArticleTitleExists = false;
  isLoading = false;
  isArticleAdded = false;
  image!:  File | null;
  showImagePreview = false;
  selectedImagePreview: string | ArrayBuffer | null = null;
  private articleService = inject(ArticleService);  

  resetForm() {
    this.articleForm = {
      title: '',
      content: '',
      imageFileName: '',

    };
    this.isArticleAdded = false;
    this.isArticleTitleExists = false;
    this.isLoading = false;
  }

  submitArticle() {
    const formData = new FormData();
    if (this.image) {
      formData.append('image', this.image);
    }    
    formData.append('article', new Blob([JSON.stringify(this.articleForm)], { type: 'application/json' }));
    this.articleService.createArticle(formData).subscribe({
      next: (response) => {
        console.log('Article created successfully', response);
        this.isLoading = false;
        this.isArticleAdded = true;
        this.image = null;
        this.showImagePreview = false;
        this.selectedImagePreview = null;
      },
      error: (error) => {
        console.error('Error creating article', error);
        if (error.error.error === "Article with this title already exists") {
          this.isArticleTitleExists = true;
        }
        this.isLoading = false;
      }
    });
    this.isLoading = true;
    this.isArticleTitleExists = false;

  }

  onImageChange(event: any) {
    this.image = event.target.files[0];
    if (this.image) {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedImagePreview = reader.result;
      };
      reader.readAsDataURL(this.image);
      this.showImagePreview = true;
    }
  }
}
