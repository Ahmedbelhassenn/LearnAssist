import { Component, OnInit } from '@angular/core';
import { FormsModule,  } from '@angular/forms';
import { CommonModule, NgIf } from '@angular/common';
import { FormationService } from '../../../services/formation/formation.service';
import { ServiceService } from '../../../services/service.service';
import { FilesService } from '../../../services/files/files.service';
@Component({
  selector: 'app-edit-formation',
  standalone: true,
  imports: [FormsModule, NgIf, CommonModule],
  templateUrl: './edit-formation.component.html',
  styleUrl: './edit-formation.component.css'
})
export class EditFormationComponent implements OnInit {



  isFormationTitleExists=false;
  selectedImagePreview: string | ArrayBuffer | null = null;
  profilePhotoUrl='';
  closeModal() {
    this.isFormationUpdated = false;
  }

  isFormationUpdated = false;
  image!:  File | null;
  videoFile!: File | null;
  myformation : any= {
    title: '',
    description: '',
    imageUrl: '',
    price: '',
    formationDuration: '',
    formationLevel: '',
    videoUrl: '',
    emailInstructor:'',
    formationStatus:'draft',
    imageFileName:'',
    videoFileName:''
  };

  constructor(private formationService: FormationService, private serviceService: ServiceService,
    private fileService: FilesService
  ){}

  ngOnInit(): void {
      this.getFormation();
  }

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

  getFormation(){
    const id = localStorage.getItem('idFormation') || '';
    this.formationService.getFormationById(id).subscribe({
      next: (response)=>{
        this.myformation=response;
        this.fileService.getFormationImage(this.myformation.imageFileName).subscribe(
          (blob) => {
            const objectURL = URL.createObjectURL(blob);
            this.selectedImagePreview = objectURL;
          },
          (error) => {
            console.error("Erreur lors du chargement de l'image", error);
          }
        );
      },
      
      error: (error)=>{
        console.log("error", error)
      }
    })
  }


  // Soumission du formulaire
  editFormation() {
    const id = localStorage.getItem('idFormation') || '';
    const formData = new FormData();
    if (this.image) {
      formData.append('image', this.image);
    } 
    if( this.videoFile){
      formData.append("video", this.videoFile)
    }
    formData.append('formation', new Blob([JSON.stringify(this.myformation)], { type: "application/json" }));
    console.log(formData)
    this.formationService.updateFormation(id,formData).subscribe({
      next: (response)=>{
        console.log('formation updated successfully');
        this.isFormationUpdated=true;
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
