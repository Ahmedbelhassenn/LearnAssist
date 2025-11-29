import { Component, NgModule, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Participant } from '../../../models/participant.model';
import { NgIf } from '@angular/common';
import { ServiceService } from '../../../services/service.service';
import { ParticipantService } from '../../../services/participant/participant.service';
import { ParticipantNavbarComponent } from '../participant-navbar/participant-navbar.component';

@Component({
  selector: 'app-edit-participant-profile',
  standalone: true,
  imports: [FormsModule,NgIf],
  templateUrl: './edit-participant-profile.component.html',
  styleUrl: './edit-participant-profile.component.css'
})
export class EditParticipantProfileComponent implements OnInit {
  id: string = '';
  oldPassword: string = '';
  isProfileModified=false;
  isOldPasswordtrue=true;
  participant: Participant = {
    firstName: "",
    lastName: "",
    phone: "",
    email: "",
    gender: "",
    dateOfBirth: '',
    city: "",
    educationLevel: "",
    password: "",
    confirmPassword: '',
    profilePhotoUrl: ''
  };
isOldPasswordFilled=true;
arePasswordsSame= true;

  constructor(private service: ServiceService, private participantService: ParticipantService) {}

  ngOnInit(): void {
    this.loadInformations();
  }

  loadInformations() {
    this.participantService.getParticipantInformation().subscribe({
      next: (response) => {
        this.participant.firstName=response.firstName;
        this.participant.lastName=response.lastName;
        this.participant.educationLevel=response.educationLevel;
        this.participant.phone=response.phone;
        this.participant.gender=response.gender;
      },
      error: (error) => {
        console.log("error:",error);
      }
    });
  }


  onSubmit() : void{
    if(this.oldPassword===''){
       this.isOldPasswordFilled= false;
    }
    else if (this.participant.confirmPassword!=this.participant.password){
      this.arePasswordsSame= false;
      this.isOldPasswordFilled= true;
    }
    else{
      this.arePasswordsSame= true;
      this.isOldPasswordFilled= true;
      this.participantService.ModifyParticipantProfile(this.participant, this.oldPassword).subscribe({
        next: (response) => {
          console.log("Mise à jour réussie");
          this.isOldPasswordtrue=true;
          this.isProfileModified=true;
        },
        error: (error) => {
          if(error.error.error==='Wrong old password'){
            this.isOldPasswordtrue=false;
            console.log("Ancien Mot de Passe incoorect!")
          }
          else{
            console.log("Erreur lors de la mise à jour :", error)
            alert("Erreur lors de la mise à jour :")
          }
        }
      });
    }
    
  }
}
