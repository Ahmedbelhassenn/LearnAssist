import { NgFor, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { LucideAngularModule, MapPin, Mail, Phone, Star, BookOpen } from 'lucide-angular';
import { FilesService } from '../../../services/files/files.service';
import { FormationService } from '../../../services/formation/formation.service';
import { InstructorService } from '../../../services/instructor/instructor.service';
import { ServiceService } from '../../../services/service.service';
import { NavbarComponent } from "../navbar/navbar.component";
import { ConnexionComponent } from "../../connexion/connexion.component";

interface Instructor {
  id: string
  firstName: string
  lastName: string
  email: string
  bio: string
  speciality: string
  photoUrl: string
  gender: string
  phone : string 
  city : string 
  dateOfBirth : string
  displayPhotoUrl?:string
}

interface formation{
  formationDuration: string
  instructorName: string
  formationLevel: string
  price: string
  imageUrl: string
  imageFileName: string
  id: string
  title: string
  displayImageUrl?: string
  isNew?: boolean
}

@Component({
  selector: 'app-instructors-details',
  standalone: true,
  imports: [NgFor, NgIf, LucideAngularModule, NavbarComponent, ConnexionComponent],
  templateUrl: './instructors-details.component.html',
  styleUrl: './instructors-details.component.css'
})
export class InstructorsDetailsComponent {
  instructor !: Instructor
    formations: formation[]=[];
    icons = {MapPin, Mail, Phone, Star, BookOpen};

    constructor(private serviceService:ServiceService, private router: Router,private route: ActivatedRoute,
        private formationService: FormationService, private instructorService: InstructorService,private fileService: FilesService){
      
      }

      ngOnInit(): void {
          this.getInstructorDetails();
      }

    getInstructorCourses() {
      this.formationService.getFormationByInstructorEmail(this.instructor.email).subscribe({
        next: (response) => {
          this.formations = response.information.map((course: formation) => {
            this.fileService.getFormationImage(course.imageFileName).subscribe(
              (blob) => {
                const objectURL = URL.createObjectURL(blob);
                course.displayImageUrl = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement de l'image", error);
                course.displayImageUrl = 'default-course.png';
              }
            );
            return course;
          });
        },
        error: (error) => {
          console.log('Erreur lors du chargement des cours', error);
        }
      });
    }

    getInstructorDetails(): void {
      const id = this.route.snapshot.paramMap.get('id');
      if (id!=null){
        this.instructorService.getInstructorDetails(id).subscribe({
        next: (response) => {
          this.instructor = response.details
            if (this.instructor.photoUrl != null) {
              this.serviceService.getProfilePicture(this.instructor.photoUrl).subscribe(
                (blob) => {
                  const objectURL = URL.createObjectURL(blob);
                  this.instructor.displayPhotoUrl = objectURL;
                },
                (error) => {
                  console.error("Erreur lors du chargement de l'image", error);
                }
              );
            }
            else if (this.instructor.gender==="M") {
              this.instructor.displayPhotoUrl="men.jpg"
            }else {
              this.instructor.displayPhotoUrl="female.jpg"
            }
            this.getInstructorCourses()
            return this.instructor;
        },
        error: (error) => {
          console.log(error);
        }
      });
      }
      
      
    }

    goToFormationDetails(id: string) {
      localStorage.setItem('idFormation',id);
      this.router.navigateByUrl('/participant/formation-details');
    }

    calculateAge(birthDate: string): number {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return age;
  }
  isMobileMenuOpen = false;
  isLogin= false;

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
  onLogin() {
    this.isLogin=!this.isLogin;
  }
}
