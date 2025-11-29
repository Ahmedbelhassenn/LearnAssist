import { Component, OnInit } from '@angular/core';
import { ServiceService } from '../../../services/service.service';
import { NgIf } from '@angular/common';
import { ParticipantNavbarComponent } from "../participant-navbar/participant-navbar.component";

@Component({
  selector: 'app-profile-picture',
  standalone: true,
  imports: [NgIf],
  templateUrl: './profile-picture.component.html',
  styleUrls: ['./profile-picture.component.css'] // Correction ici (styleUrl → styleUrls)
})
export class ProfilePictureComponent  implements OnInit{
  selectedFile!: File | null;
  profileImageUrl: string = 'men.jpg'; 
  isProfilePhotoModified: boolean=false;
  profilePhotoUrl= localStorage.getItem("photoName");

  constructor(private Service:ServiceService) {}
  ngOnInit() {
    this.uploadProfilePicture();
  }

  onFileSelected(event: Event) {
    const fileInput = event.target as HTMLInputElement;
    if (fileInput.files && fileInput.files.length > 0) {
      this.selectedFile = fileInput.files[0];

      // Vérification du type de fichier
      if (!this.selectedFile.type.startsWith('image/')) {
        alert("Veuillez sélectionner une image valide.");
        this.selectedFile = null;
        return;
      }

      // Aperçu de l'image
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.profileImageUrl = e.target.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }
  

  modifyProfilePicture() {
    if (!this.selectedFile) {
      alert('Veuillez sélectionner une image.');
      return;
    }
    const formData = new FormData();
    const id= localStorage.getItem('userId') || '';
    const role=localStorage.getItem('userRole')|| '';
    formData.append('file', this.selectedFile);
    formData.append('id', id);
    formData.append('role',role);
    this.Service.uploadProfilePhoto(formData)
      .subscribe({
        next: (response) => {
          this.isProfilePhotoModified=true;
          localStorage.setItem('photoName',response.fileName);
        },
        error: (error) => {
          console.error('Erreur lors de l’upload :', error);
          alert('Une erreur est survenue lors du téléchargement.');
        }
    });
    setTimeout(() => {
      window.location.reload();
    }, 1000); 
  }
  uploadProfilePicture(): void {
    if (this.profilePhotoUrl) {
      this.Service.getProfilePicture(this.profilePhotoUrl).subscribe(
        (blob) => {
          const objectURL = URL.createObjectURL(blob);
          this.profileImageUrl=objectURL
        },
        (error) => {
          console.error('Erreur lors du chargement de l\'image', error);
        }
      );
    }
  }
}
