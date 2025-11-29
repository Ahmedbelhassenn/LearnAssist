import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { AuthService } from '../../../services/auth-service/auth.service';

@Component({
  selector: 'app-participant-login',
  standalone: true,
  imports: [FormsModule,NgIf, RouterLink],
  templateUrl: './participant-login.component.html',
  styleUrl: './participant-login.component.css'
})
export class ParticipantLoginComponent {
  credentials={
    email: '',
    password: ''
  }
  errorMessage='';
  constructor(private authService:AuthService,private router: Router){}
  onSubmit() {
    this.authService.loginParticipant(this.credentials).subscribe({
      next: (response) => {
        console.log("Connexion réussie");
        debugger
        this.errorMessage = '';
        localStorage.setItem('userToken',response.jwtToken);
        localStorage.setItem('userEmail',response.email);
        localStorage.setItem('userId',response.id);
        localStorage.setItem('userRole', response.roles[0] );
        localStorage.setItem('userGender', response.gender);
        localStorage.setItem('photoName',response.photoName);
        debugger
        this.router.navigate(['/participant/home-page']);
        debugger
      },
      error: (error) => {
        console.log('Échec de la connexion', error);
        if (error.error.error === "User not found") {
          this.errorMessage = "Cet email n'existe pas, merci de vérifier votre adresse email !";
        } else if(error.error.error==="Les identifications sont erronées"){
          this.errorMessage = "Mot de passe incorrect"
        } else {
          this.errorMessage = "Une erreur serveur est survenue. Veuillez réessayer plus tard.";
        }
      }
    });
  }
  
  
}
