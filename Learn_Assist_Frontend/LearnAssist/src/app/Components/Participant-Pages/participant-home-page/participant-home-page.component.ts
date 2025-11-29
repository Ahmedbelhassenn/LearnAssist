import { Component, inject, OnInit } from '@angular/core';
import { ServiceService } from '../../../services/service.service';
import { DatePipe, NgFor, NgIf, SlicePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormationService } from '../../../services/formation/formation.service';
import { ParticipantService } from '../../../services/participant/participant.service';
import { User, LucideAngularModule } from 'lucide-angular';
import { FilesService } from '../../../services/files/files.service';
import { Article } from '../../../models/article.model';
import { ArticleService } from '../../../services/article/article.service';

interface formation {
  formationDuration: string,
  instructorName: string,
  formationLevel: string,
  price: string,
  imageUrl: string,
  imageFileName: string,
  id: string,
  title: string
}

@Component({
  selector: 'app-participant-home-page',
  standalone: true,
  imports: [NgFor, RouterLink, LucideAngularModule, NgIf, DatePipe, SlicePipe],

  templateUrl: './participant-home-page.component.html',
  styleUrl: './participant-home-page.component.css'
})
export class ParticipantHomePageComponent implements OnInit {

  formations: formation[] = [];
  id: String = '';
  username = 'UserName';
  displayedFormations: any[] = [];
  selectedImage?: string | null = null;
  icons = { User }
  constructor(private serviceService: ServiceService, private router: Router,
    private formationService: FormationService, private participantService: ParticipantService, private fileService: FilesService) {

  }

  private articleService = inject(ArticleService);
  ngOnInit(): void {
    this.getFormations();
    this.getInformation();
    this.loadInstructorArticles();
  }
  getFormations() {
    this.formationService.getCourses().subscribe({
      next: (response) => {
        this.formations = response.map((course: formation) => {
          if (course.imageFileName != null) {
            this.fileService.getFormationImage(course.imageFileName).subscribe(
              (blob) => {
                const objectURL = URL.createObjectURL(blob);
                course.imageUrl = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement de l'image", error);
              }
            );
          }
          return course;

        });
        this.displayedFormations = this.formations.slice(0, 4); // On prend que les 2 premières
      },
      error: (error) => {
        console.log('Erreur lors du chargement des cours', error);
      }
    });

  }

  getInformation(): void {
    this.participantService.getParticipantInformation().subscribe({
      next: (response) => {
        this.username = `${response.firstName} ${response.lastName}`;
        localStorage.setItem('photoName', response.profilePhotoUrl);
      },
      error: (error) => {
        console.log('Erreur lors du chargement des informations utilisateur', error);
      }
    });
  }

  goToFormationDetails(id: string) {
    this.router.navigate(['/participant/formation-details', id]);
  }

  currentYear: number = new Date().getFullYear();

  idArticle = '';
  articles: Article[] = [];
  loadInstructorArticles() {
    this.articleService.getAllArticles().subscribe({
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
  openImage(url: string, event?: Event) {
    event?.stopPropagation();   // sécurité
    this.selectedImage = url;
    // (optionnel) bloquer le scroll du body pendant la modal
    // document.body.style.overflow = 'hidden';
  }

  closeImage() {
    this.selectedImage = null;
  }

}
