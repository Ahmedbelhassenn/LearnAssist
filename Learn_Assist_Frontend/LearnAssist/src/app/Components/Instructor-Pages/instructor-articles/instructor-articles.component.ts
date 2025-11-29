import { Component, inject } from '@angular/core';
import { Article } from '../../../models/article.model';
import { ArticleService } from '../../../services/article/article.service';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf, DatePipe } from '@angular/common';
import { AddArticleComponent } from "../../formation-Pages/add-article/add-article.component";
import { FilesService } from '../../../services/files/files.service';
import { EditArticleComponent } from "../../formation-Pages/edit-article/edit-article.component";

@Component({
  selector: 'app-instructor-articles',
  standalone: true,
  imports: [FormsModule, NgFor, NgIf, DatePipe, AddArticleComponent, EditArticleComponent],
  templateUrl: './instructor-articles.component.html',
  styleUrl: './instructor-articles.component.css'
})
export class InstructorArticlesComponent {
  articles: Article[] = [];
  isArticleModalOpen = false;
  showDeleteModal = false;
  isEditArticleModalOpen = false;
  selectedArticleId = '';
  idArticle = '';

  newArticle: Article = { title: '', content: '', imageFileName: '' };

  private articleService = inject(ArticleService);
  private fileService = inject(FilesService);
  ngOnInit(): void {
    this.loadInstructorArticles();
  }

  closeArticleModal() {
    this.isArticleModalOpen = false;
    this.loadInstructorArticles(); // Recharger les articles après la fermeture du modal
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

  openDeleteModal(id?: string ): void {
    if(id){
      this.idArticle = id;
      this.showDeleteModal = true;
    }
    else{
      console.error("ID de l'article manquant pour la suppression");
    }
    
  }
  openEditDeleteModal(id?: string ): void {
    if(id){
      this.selectedArticleId = id;
      this.isEditArticleModalOpen = true;
    }
    else{
      console.error("ID de l'article manquant pour la modification");
    } 
  }


 closeEditArticleModal() {
    this.isEditArticleModalOpen = false;
    this.loadInstructorArticles(); // Recharger les articles après la fermeture du modal
  }
  deleteArticle(): void {
    const id = this.idArticle;
    if (!id) {
      console.error('ID de l\'article manquant pour la suppression');
      return;
    }
    this.articleService.deleteArticle(id).subscribe({
      next: () => {
        console.log('Article deleted successfully');
        this.showDeleteModal = false;
        this.loadInstructorArticles();
      },
      error: (error) => {
        console.error('Error deleting article:', error);
      }
    });
  }

  
}
