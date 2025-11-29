import { NgFor, NgIf } from '@angular/common';
import { Component, ElementRef, ViewChild } from '@angular/core';
import { SafeUrlPipe } from '../../../safe-url.pipe';
import { FormationService } from '../../../services/formation/formation.service';
import { Formation } from '../../../models/formation.model';
import { ServiceService } from '../../../services/service.service';
import { Router, RouterLink } from '@angular/router';
import { CourseService } from '../../../services/course/course.service';
import { FilesService } from '../../../services/files/files.service';
import { FormsModule } from '@angular/forms';
import { ParticipantService } from '../../../services/participant/participant.service';
import { InscriptionFormationService } from '../../../services/inscriptionFormation/inscription-formation.service';

interface Course{
  id: string,
  title: string,
}

@Component({
  selector: 'app-participant-formation-details',
  standalone: true,
  imports: [NgFor, SafeUrlPipe, FormsModule, NgIf],
  templateUrl: './participant-formation-details.component.html',
  styleUrl: './participant-formation-details.component.css'
})
export class ParticipantFormationDetailsComponent {

  instructorProfilePhoto='profile.jpg'
    formation: Formation= {
      formationDuration: '',
      formationLevel: '',
      price: '',
      title: '',
      videoUrl: '',
      description: '',
      contents: [],
      imageUrl: '',
      formationStatus: '',
      instructorName: '',
      instructorProfilePhoto: '',
      instructorBio: '',
      videoFileName: ''
    };
  
    course: Course[]=[];
    isInscriptionFormOpened=false;
    isRequestPending: boolean = false;
    inscriptionDemandId = '';
    isRequestApproved = false;

  
    participationData = {
      participantName: '',
      participantEmail: '',
      participantPhone: '',
      formationId: ''
    };
    
    @ViewChild('inscriptionBtn') inscriptionBtn!: ElementRef;
    constructor(private formationService: FormationService, private serviceService: ServiceService, private participantService: ParticipantService,
      private courseService: CourseService, private router: Router, private fileService: FilesService, private inscriptionFormationService: InscriptionFormationService
    ) {}
  
    ngOnInit(): void {
      this.getFormation();
      this.getCourses();
      this.getInformation();
      this.getInscriptionFormationStatus();
    }
    getFormation(){
      const id = localStorage.getItem('idFormation') || '';
      this.formationService.getFormationById(id).subscribe({
        next: (response)=>{
          this.formation=response;
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

    getInformation(): void {
    this.participantService.getParticipantInformation().subscribe({
      next: (response) => {
        this.participationData.participantName = `${response.firstName} ${response.lastName}`;
        this.participationData.participantEmail = response.email
        this.participationData.participantPhone = response.phone
      },
      error: (error) => {
        console.log('Erreur lors du chargement des informations utilisateur', error);
      }
    });
  }
    getCourses(): void{
      const id=localStorage.getItem('idFormation') || '';
      this.courseService.getCourses(id).subscribe({
        next: (response)=>{
          this.course=response.map((course:Course)=>{
            course.title=this.serviceService.capitalizeFirstLetter(course.title);
            return course
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
      this.router.navigate(['/participant/course-details', courseId]);
    }
  
    

    handleCourseClick(courseId: string) {
      if (this.isRequestApproved) {
        this.goToCourseDetails(courseId);
      } else {
        // Animation douce vers le bouton d’inscription
        this.inscriptionBtn.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
        
        // Optionnel : animation visuelle (secousse ou glow)
        this.inscriptionBtn.nativeElement.classList.add('animate-bounce');
        setTimeout(() => {
          this.inscriptionBtn.nativeElement.classList.remove('animate-bounce');
        }, 1000);
      }
    }


    submitParticipation() {
      this.participationData.formationId=localStorage.getItem('idFormation') || '';
      this.inscriptionFormationService.addInscriptionDemand(this.participationData).subscribe({
        next : (response)=>{
          console.log("ok");
          this.isRequestPending = true;   // La demande est maintenant en attente
          this.closeInscriptionForm();    // Fermer le modal
        },
        error : (error)=>{
          alert("error lors d'envoi de demande d'inscription ")
          this.closeInscriptionForm();
        }
      })


    }

    getInscriptionFormationStatus(){
      const id = localStorage.getItem('idFormation') || ''
      this.inscriptionFormationService.getInscriptionFormationStatus(id).subscribe({
        next : (response)=>{
          if (response && Object.keys(response).length>0){
            this.inscriptionDemandId= response.id
            if (response.status === "Pending"){
              this.isRequestPending=true;
            }
            if(response.status === "Approved"){
              this.isRequestApproved = true
              
            }
          }
        },

        error : (error)=>{
          console.log(error)
        }
      })
    }

    
    cancelParticipationRequest() {
      this.inscriptionFormationService.deleteInscriptionDemand(this.inscriptionDemandId).subscribe({
        next : (response)=> {
          console.log("ok");
          this.isRequestPending = false; 
        },
        error:(err)=> {
          alert("il y a un erreur lors d' annulation de demande")  
        },

      })

      // repasser en mode "peut s'inscrire"
    }


    closeInscriptionForm(): void{
        this.isInscriptionFormOpened=!this.isInscriptionFormOpened
    }


    openInscriptionForm(): void{
        this.isInscriptionFormOpened=!this.isInscriptionFormOpened
    }

    
}


