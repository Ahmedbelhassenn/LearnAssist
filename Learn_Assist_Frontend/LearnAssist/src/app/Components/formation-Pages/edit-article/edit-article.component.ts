import { NgIf, SlicePipe } from '@angular/common';
import { Component, EventEmitter, inject, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Article } from '../../../models/article.model';
import { ArticleService } from '../../../services/article/article.service';
import { FilesService } from '../../../services/files/files.service';

@Component({
  selector: 'app-edit-article',
  standalone: true,
  imports: [FormsModule, NgIf, SlicePipe],
  templateUrl: './edit-article.component.html',
  styleUrl: './edit-article.component.css'
})
export class EditArticleComponent implements OnInit {
  @Input() articleId!: string; 

  articleForm: Article={title:'', content:'', publishedAt: new Date()};
    isArticleTitleExists = false;
    isLoading = false;
    isArticleEdited = false;
    image!:  File | null;
    showImagePreview = false;
    selectedImagePreview: string | ArrayBuffer | null = null;
    
    
    private articleService = inject(ArticleService); 
    private fileService = inject(FilesService); 
    ngOnInit(): void {
      this.loadArticle();
    }
  
    loadArticle() {
      this.articleService.getArticleById(this.articleId).subscribe({
        next: (article) => {
          this.articleForm = article;
          if (article.imageFileName) {
            this.fileService.getArticleImage(article.imageFileName).subscribe(
              (blob) => {
                  const objectURL = URL.createObjectURL(blob);
                  this.selectedImagePreview = objectURL;
                  this.showImagePreview = true;
                },
                (error) => {
                  console.error("Erreur lors du chargement de l'image", error);
                }
             )
          }
        },
      });
    }

  
    submitArticle() {
      const formData = new FormData();
      if (this.image) {
        formData.append('image', this.image);
      }    
      formData.append('article', new Blob([JSON.stringify(this.articleForm)], { type: 'application/json' }));
      this.articleService.editArticle(this.articleId,formData).subscribe({
        next: (response) => {
          console.log('Article edited successfully', response);
          this.isLoading = false;
          this.isArticleEdited = true;
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
