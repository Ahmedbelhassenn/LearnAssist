import { NgClass, NgIf } from '@angular/common';
import { Component } from '@angular/core';
import { RouterLinkActive, RouterLink } from '@angular/router';
import { ConnexionComponent } from "../../connexion/connexion.component";

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [NgClass, RouterLinkActive, RouterLink, ConnexionComponent, NgIf],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {
  isMobileMenuOpen = false;
  isLogin= false;

  toggleMobileMenu() {
    this.isMobileMenuOpen = !this.isMobileMenuOpen;
  }
  onLogin() {
    this.isLogin=!this.isLogin;
  }
  
}
