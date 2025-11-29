import { NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth-service/auth.service';

@Component({
  selector: 'app-connexion',
  standalone: true,
  imports: [FormsModule, NgIf, RouterLink],
  templateUrl: './connexion.component.html',
  styleUrl: './connexion.component.css'
})
export class ConnexionComponent {
  isParticipantLogin = true;
  isInstructorLogin = false;
  credentials = { email: '', password: '' };
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router){}

  toggleLogin(type: 'participant' | 'instructor') {
    this.isParticipantLogin = type === 'participant';
    this.isInstructorLogin = type === 'instructor';
    this.errorMessage='';
  }

  onSubmit() {
    console.log("Connexion en cours pour", this.isParticipantLogin ? 'Participant' : 'Formateur');
    if(this.isParticipantLogin){
      this.authService.loginParticipant(this.credentials).subscribe({
        next: (response) => {
          console.log("Connexion réussie");
          this.errorMessage = '';
          localStorage.setItem('userToken',response.jwtToken);
          localStorage.setItem('userEmail',response.email);
          localStorage.setItem('userId',response.id);
          localStorage.setItem('userRole', response.roles[0] );
          localStorage.setItem('userGender', response.gender);
          localStorage.setItem('photoName',response.photoName);
          this.router.navigate(['/participant/home-page']);
        },
        error: (error) => {
          console.log('Échec de la connexion', error);
          if (error.error.error === "User not found" || error.error.error==="Les identifications sont erronées") {
            this.errorMessage = "Adresse e-mail ou mot de passe incorrect !";
          }else {
            this.errorMessage = "Une erreur serveur est survenue. Veuillez réessayer plus tard.";
          }
        }
      });
    }
    else{
      this.authService.loginInstructor(this.credentials).subscribe({
        next: (response) => {
          console.log("Connexion réussie");
          this.errorMessage = '';
          localStorage.setItem('userToken',response.jwtToken);
          localStorage.setItem('userEmail',response.email);
          localStorage.setItem('userId',response.id);
          localStorage.setItem('userRole', response.roles[0] );
          localStorage.setItem('userGender', response.gender);
          localStorage.setItem('photoName',response.photoName);
          this.router.navigate(['/instructor/home-page']);
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
}
