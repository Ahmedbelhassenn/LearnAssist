import { Component } from '@angular/core';
import { Instructor } from '../../../models/instructor.model';
import { ServiceService } from '../../../services/service.service';
import { Router, RouterLink } from '@angular/router';
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InstructorService } from '../../../services/instructor/instructor.service';
import { AuthService } from '../../../services/auth-service/auth.service';

@Component({
  selector: 'app-instructor-signup',
  standalone: true,
  imports: [FormsModule, NgIf,RouterLink],
  templateUrl: './instructor-signup.component.html',
  styleUrl: './instructor-signup.component.css'
})
export class InstructorSignupComponent {
  errorMessage: string = ''; 
  instructor: Instructor= {
     firstName: "",
     lastName: "",
     phone: "",
     email: "",
     gender: "",
     dateOfBirth: undefined , 
     city: "",
     speciality: "",
     password: "",
     confirmPassword: ''
   }
 
   constructor(private authService:AuthService, private router: Router){}
 
   OnSubmit() {
     this.authService.registerInstructor(this.instructor).subscribe({
       next: (response) => {
        console.log('Registration Successful', response);
        this.errorMessage = ''; 
        localStorage.setItem('userToken',response.jwtToken);
        localStorage.setItem('userEmail',response.email);
        localStorage.setItem('userId',response.id);
        localStorage.setItem('userRole', response.roles[0] );
        localStorage.setItem('userGender', response.gender);
        this.router.navigate(['/instructor/home-page']);
       },
       error: (error) => {
         console.log('Registration Failed', error);
         if (error.status === 400 && error.error.includes('Email already registered')) {
           this.errorMessage = 'Cet email est déjà utilisé. Veuillez en choisir un autre.';
         } else {
           this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
         }
       },
     });
   }
}
