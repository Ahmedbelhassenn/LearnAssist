import { Component } from '@angular/core';
import { InstructorService } from '../../../services/instructor/instructor.service';
import { ServiceService } from '../../../services/service.service';
import { FormsModule } from '@angular/forms';
import { NgFor, NgIf } from '@angular/common';
import { Route } from 'lucide-angular';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from "../navbar/navbar.component";

interface Instructor {
  id: string
  firstName: string
  lastName: string
  email: string
  bio: string
  speciality: string
  photoUrl: string
  gender: string
  displayPhotoUrl?:string
}
@Component({
  selector: 'app-instructors',
  standalone: true,
  imports: [FormsModule, NgIf, NgFor, RouterLink, NavbarComponent],
  templateUrl: './instructors.component.html',
  styleUrl: './instructors.component.css'
})
export class InstructorsComponent {
instructors: Instructor[] = [];
  searchInstructor = '';

  constructor(private instructorService: InstructorService, private service: ServiceService) {}

  ngOnInit(): void {
    this.getInstructorList();
  }

  getInstructorList(): void {
    this.instructorService.getInstructorList().subscribe({
      next: (response) => {
        this.instructors = response.list.map((instructor: Instructor) => {
          if (instructor.photoUrl != null) {
            this.service.getProfilePicture(instructor.photoUrl).subscribe(
              (blob) => {
                const objectURL = URL.createObjectURL(blob);
                instructor.displayPhotoUrl = objectURL;
              },
              (error) => {
                console.error("Erreur lors du chargement de l'image", error);
              }
            );
          }
          else if (instructor.gender==="M") {
            instructor.displayPhotoUrl="men.jpg"
          }else {
            instructor.displayPhotoUrl="female.jpg"
          }
          return instructor;
        });
      },
      error: (error) => {
        console.log(error);
      }
    });
  }

  // ✅ Getter pour filtrer dynamiquement les instructeurs
  get filteredInstructors(): Instructor[] {
    const term = this.searchInstructor.toLowerCase();
    return this.instructors.filter(instructor =>
      instructor.firstName.toLowerCase().includes(term) ||
      instructor.lastName.toLowerCase().includes(term) ||
      instructor.speciality.toLowerCase().includes(term)
    );
  }

  viewInstructorProfile(id: string) {
    console.log('Voir le profil du formateur:', id);
    // TODO: Naviguer vers la page de détail
    // this.router.navigate(['/instructors', id]);
  }
}
