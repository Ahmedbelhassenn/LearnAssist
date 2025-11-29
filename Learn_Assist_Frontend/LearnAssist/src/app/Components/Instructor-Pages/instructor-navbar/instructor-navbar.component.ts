import { Component, HostListener, OnInit } from '@angular/core';
import { ServiceService } from '../../../services/service.service';
import {  NgClass, NgIf } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { LucideAngularModule, LogOut, User, Edit ,Image,Book  } from 'lucide-angular';
@Component({
  selector: 'app-instructor-navbar',
  standalone: true,
  imports: [LucideAngularModule,NgIf,RouterLink, RouterLinkActive, NgClass],
  templateUrl: './instructor-navbar.component.html',
  styleUrl: './instructor-navbar.component.css'
})
export class InstructorNavbarComponent implements OnInit {
  icons = { LogOut, User, Edit,Image,Book};
  profileImageUrl = 'men.jpg'; 
  profilePhotoName= localStorage.getItem('photoName');
  isDropdownOpen = false;

  constructor(private serviceService:ServiceService, private router: Router){}
    
  ngOnInit() {
    this.uploadProfilePicture()
  }

  uploadProfilePicture(): void {
    if (this.profilePhotoName!=null && this.profilePhotoName!='null') {
      this.serviceService.getProfilePicture(this.profilePhotoName).subscribe(
        (blob) => {
          const objectURL = URL.createObjectURL(blob);
          this.profileImageUrl=objectURL
        },
        (error) => {
          console.error('Erreur lors du chargement de l\'image', error);
        }
      );
    }
    else{
      const gender=localStorage.getItem('userGender');
      if (gender==='F'){
        this.profileImageUrl='female.jpg'
      }
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    const target = event.target as HTMLElement;
    if (!target.closest('.relative')) {
      this.isDropdownOpen = false;
    }
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  editProfile(): void {
    this.isDropdownOpen = false;
    this.router.navigate(['/profile/edit']);
  }

  editPhoto(): void {
    this.isDropdownOpen = false;
    this.router.navigate(['/profile/photo']);
  }

  isMobileMenuOpen = false;

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
  
  logout(): void {
    this.isDropdownOpen = false;
    this.showLogoutConfirmation = true;
  }
  showLogoutConfirmation = false;


  confirmLogout() {
    localStorage.removeItem('userToken'); 
    localStorage.removeItem('userId');
    localStorage.removeItem('courseId');
    localStorage.removeItem('chapterId');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    localStorage.removeItem('userGender');
    localStorage.removeItem('photoName');
    localStorage.removeItem('idFormation');
    this.router.navigate(['/home']);
    this.showLogoutConfirmation = false;
  }

  cancelLogout() {
    this.showLogoutConfirmation = false;
  }
}
