import { Component } from '@angular/core';
import { FormsModule,  } from '@angular/forms';
import { CommonModule, NgIf } from '@angular/common';
import { FormationService } from '../../../services/formation/formation.service';
import { RouterLink } from '@angular/router';
@Component({
  selector: 'app-add-formation',
  standalone: true,
  imports: [FormsModule, NgIf, CommonModule, RouterLink],
  templateUrl: './add-formation.component.html',
  styleUrl: './add-formation.component.css'
})
export class AddFormationComponent {

  isFormationTitleExists=false;
  selectedImagePreview: string | ArrayBuffer | null = null;
  closeModal() {
    this.isFormationAdded = false;
  }

  isFormationAdded = false;
  image!:  File | null;
  videoFile!: File | null;
  newFormation : any= {
    title: '',
    description: '',
    imageUrl: '',
    price: '',
    formationDuration: '',
    formationLevel: '',
    videoUrl: '',
    emailInstructor:'',
    formationStatus:'draft'
  };

  constructor(private formationService: FormationService){}


  onImageChange(event: any) {
    this.image = event.target.files[0];
    if (this.image) {
      const reader = new FileReader();
      reader.onload = () => {
        this.selectedImagePreview = reader.result;
      };
      reader.readAsDataURL(this.image);
    }
  }

  onVideoChange(event: any) {
    const file = event.target.files[0];
    if (file) {
      this.videoFile = file;
    }
  }


  // Soumission du formulaire
  submitFormation() {
    if (!this.image) {
      return;
    }
    if (!this.videoFile) {
      return;
    }
    const formData = new FormData();
    formData.append('image', this.image);
    formData.append('video', this.videoFile);
    this.newFormation.emailInstructor=localStorage.getItem('userEmail') || '';
    formData.append('formation', new Blob([JSON.stringify(this.newFormation)], { type: "application/json" }));
    this.formationService.addFormation(formData).subscribe({
      next: (response)=>{
        console.log('formation added successfully');
        localStorage.setItem('idFormation', response.id)
        this.isFormationAdded=true;
      },
      error: (error)=>{
        if (error.error.message==='Formation with this title already exists'){
            this.isFormationTitleExists=true;
        }
        console.log("error", error)
      }
    })
  }

   
}
