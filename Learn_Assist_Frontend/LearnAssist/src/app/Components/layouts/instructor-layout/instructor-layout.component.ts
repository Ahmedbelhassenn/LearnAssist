import { Component } from '@angular/core';
import { InstructorNavbarComponent } from "../../Instructor-Pages/instructor-navbar/instructor-navbar.component";
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-instructor-layout',
  standalone: true,
  imports: [InstructorNavbarComponent, RouterOutlet],
  templateUrl: './instructor-layout.component.html',
  styleUrl: './instructor-layout.component.css'
})
export class InstructorLayoutComponent {

}
