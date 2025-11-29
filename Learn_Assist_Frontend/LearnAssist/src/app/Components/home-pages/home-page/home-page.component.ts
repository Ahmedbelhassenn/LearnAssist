import { DatePipe, NgClass, NgFor, NgIf, SlicePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ServiceService } from '../../../services/service.service';
import { ConnexionComponent } from "../../connexion/connexion.component";
import { FormationService } from '../../../services/formation/formation.service';
import { LucideAngularModule, User } from 'lucide-angular';
import { FilesService } from '../../../services/files/files.service';
import { NavbarComponent } from "../navbar/navbar.component";
import { Article } from '../../../models/article.model';
import { ArticleService } from '../../../services/article/article.service';


interface FeatureCard{
  icon: any;
  title: string;
  description: string;
}

interface StatCard {
  number: string;
  label: string;
}

interface formation{
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
  selector: 'app-home-page',
  standalone: true,
  imports: [NgFor, NgIf, ConnexionComponent, LucideAngularModule, NavbarComponent, DatePipe, SlicePipe],
  templateUrl: './home-page.component.html',
  styleUrl: './home-page.component.css'
})
export class HomePageComponent {
  icons={User}
  isLogin= false;
  formations: formation[]=[];
  displayedFormations: formation[]=[]; 

  selectedImage?: string | null = null;

  
  constructor(private serviceService:ServiceService, private formationService: FormationService,
    private fileService: FilesService
  ){
    this.getFormations()
    this.loadInstructorArticles();
  }

  private articleService = inject(ArticleService);
  

  onLogin() {
    this.isLogin=!this.isLogin;
  }
  
  features: FeatureCard[] = [
    {
      icon: "video.png",
      title: "Cours en direct",
      description: "Participez à des sessions interactives en temps réel avec nos formateurs experts"
    },
    {
      icon: 'SIMPLE CHATBOTS.png',
      title: 'Chatbot intelligent',
      description: 'Obtenez des réponses instantanées à vos questions 24/7'
    },
    {
      icon: 'certification.png',
      title: 'Certifications',
      description: 'Obtenez des certifications reconnues pour valoriser vos compétences'
    }
  ]

  getFormations(){
    this.formationService.getCourses().subscribe({
      next: (response) => {
        this.formations = response.map((formation: formation) => {
          if(formation.imageFileName!=null){
            this.fileService.getFormationImage(formation.imageFileName).subscribe(
              (blob) => {
                const objectURL = URL.createObjectURL(blob);
                formation.imageUrl = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement de l'image", error);
              }
            );
          }
          return formation;
        });
        this.displayedFormations=this.formations.slice(0,4);
      },
      error: (error) => {
        console.log('Erreur lors du chargement des cours', error);
      }
    });
    
  }
  stats: StatCard[] = [
    { number: '10,000+', label: 'Étudiants' },
    { number: '200+', label: 'Formateurs' },
    { number: '500+', label: 'Cours' },
    { number: '50+', label: 'Pays' }
  ];

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
