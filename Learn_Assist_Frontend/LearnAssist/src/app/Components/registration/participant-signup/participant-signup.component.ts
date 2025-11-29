import { Component } from '@angular/core';
import { ServiceService } from '../../../services/service.service';
import { Router, RouterLink } from '@angular/router';
import { Participant } from '../../../models/participant.model';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthService } from '../../../services/auth-service/auth.service';

@Component({
  selector: 'app-participant-signup',
  standalone: true,
  imports: [FormsModule,NgIf,RouterLink],
  templateUrl: './participant-signup.component.html',
  styleUrl: './participant-signup.component.css'
})
export class ParticipantSignupComponent {
  termsAccepted=false;
  errorMessage: string='';
  participant: Participant={
    firstName: "",
    lastName: "",
    phone: "",
    email: "",
    gender: "",
    dateOfBirth: '' , 
    city: "",
    educationLevel: "",
    password: "",
    confirmPassword: '',
    profilePhotoUrl:''
  }

  constructor(private authService:AuthService, private router: Router){}

  onSubmit(){
    this.authService.registerParticipant(this.participant).subscribe({
      next:(response)=>{
        console.log('Registration Successful!');
        localStorage.setItem('userToken',response.jwtToken);
        localStorage.setItem('userEmail',response.email);
        localStorage.setItem('userId',response.id);
        localStorage.setItem('userRole', response.roles[0] );
        localStorage.setItem('userGender', response.gender);
        this.errorMessage='';
        this.router.navigate(['/participant/home-page']);
      },
      error: (error)=>{
        console.log('Registration failed!', error);
        if (error.error==='Email already registered!') {
          this.errorMessage = 'Cet email est déjà utilisé. Veuillez en choisir un autre.';
        } else {
          this.errorMessage = 'Une erreur est survenue. Veuillez réessayer.';
        }
      }
    })
  }

  
}
