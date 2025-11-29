import { Component } from '@angular/core';
import { Instructor } from '../../../models/instructor.model';
import { ServiceService } from '../../../services/service.service';
import { InstructorNavbarComponent } from "../instructor-navbar/instructor-navbar.component";
import { NgIf } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InstructorService } from '../../../services/instructor/instructor.service';

@Component({
  selector: 'app-edit-instructor-profile',
  standalone: true,
  imports: [ NgIf, FormsModule],
  templateUrl: './edit-instructor-profile.component.html',
  styleUrl: './edit-instructor-profile.component.css'
})
export class EditInstructorProfileComponent {
  id: string = '';
  oldPassword: string = '';
  arePasswordsSame= true;
  isProfileModified= false;
  isOldPasswordFilled= true;
  isOldPasswordtrue=true;
  instructor: Instructor = {
    firstName: "",
    lastName: "",
    phone: "",
    email: "",
    gender: "",
    dateOfBirth: '',
    city: "",
    speciality: "",
    password: "",
    confirmPassword: '',
  };

  
  constructor(private service: ServiceService,  private instructorService: InstructorService) {}

  ngOnInit(): void {
    this.loadInformations();
  }

  loadInformations() {
    this.instructorService.getInstructorInformation().subscribe({
      next: (response) => {
        this.instructor.firstName=response.firstName;
        this.instructor.lastName=response.lastName;
        this.instructor.speciality=response.speciality;
        this.instructor.phone=response.phone;
        this.instructor.gender=response.gender;
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
    else if (this.instructor.confirmPassword!=this.instructor.password){
      this.arePasswordsSame= false;
      this.isOldPasswordFilled= true;
    }
    else{
      this.arePasswordsSame= true;
      this.isOldPasswordFilled= true;
      this.instructorService.ModifyInstructorProfile(this.instructor, this.oldPassword).subscribe({
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
