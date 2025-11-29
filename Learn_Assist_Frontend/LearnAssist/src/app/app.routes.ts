import { Routes } from '@angular/router';
import { HomePageComponent } from './Components/home-pages/home-page/home-page.component';
import { ParticipantSignupComponent } from './Components/registration/participant-signup/participant-signup.component';
import { InstructorSignupComponent } from './Components/registration/instructor-signup/instructor-signup.component';
import { ParticipantHomePageComponent } from './Components/Participant-Pages/participant-home-page/participant-home-page.component';
import { participantGuard } from './guards/participant.guard';
import { ProfilePictureComponent } from './Components/Participant-Pages/profile-picture/profile-picture.component';
import { EditParticipantProfileComponent } from './Components/Participant-Pages/participant-edit-profile/edit-participant-profile.component';
import { InstructorHomePageComponent } from './Components/Instructor-Pages/instructor-home-page/instructor-home-page.component';
import { instructorGuard } from './guards/instructor.guard';
import { InstructorEditProfilePictureComponent } from './Components/Instructor-Pages/instructor-edit-profile-picture/instructor-edit-profile-picture.component';
import { EditInstructorProfileComponent } from './Components/Instructor-Pages/edit-instructor-profile/edit-instructor-profile.component';
import { AddFormationComponent } from './Components/formation-Pages/add-formation/add-formation.component';
import { AddCoursesComponent } from './Components/formation-Pages/add-courses/add-courses.component';
import { ConnexionComponent } from './Components/connexion/connexion.component';
import { ParticipantFormationDetailsComponent } from './Components/Participant-Pages/participant-formation-details/participant-formation-details.component';
import { ParticipantCourseDetailsComponent } from './Components/Participant-Pages/participant-course-details/participant-course-details.component';
import { AllFormationsComponent } from './Components/formation-Pages/all-formations/all-formations.component';
import { ParticipantAllFormationComponent } from './Components/Participant-Pages/participant-all-formation/participant-all-formation.component';
import { ParticipantLayoutComponent } from './Components/layouts/participant-layout/participant-layout.component';
import { ChatPageComponent } from './Components/Participant-Pages/chat-page/chat-page.component';
import { InstructorsListComponent } from './Components/Participant-Pages/instructors-list/instructors-list.component';
import { InstructorDetailsPageComponent } from './Components/Participant-Pages/instructor-details-page/instructor-details-page.component';
import { InstructorLayoutComponent } from './Components/layouts/instructor-layout/instructor-layout.component';
import { ApprovalRequestsComponent } from './Components/Instructor-Pages/approval-requests/approval-requests.component';
import { InstructorArticlesComponent } from './Components/Instructor-Pages/instructor-articles/instructor-articles.component';
import { InstructorCourseDetailsComponent } from './Components/Instructor-Pages/instructor-course-details/instructor-course-details.component';
import { InstructorFormationDetailsComponent } from './Components/Instructor-Pages/instructor-formation-details/instructor-formation-details.component';
import { InstructorsComponent } from './Components/home-pages/instructors/instructors.component';
import { InstructorsDetailsComponent } from './Components/home-pages/instructors-details/instructors-details.component';

export const routes: Routes = [
    {path: 'home', component: HomePageComponent},
    {path: 'instructors-list', component: InstructorsComponent},
    {path: 'instructors-list/:id', component:InstructorsDetailsComponent},
    {path: 'participant-registration', component:ParticipantSignupComponent},
    {path: 'instructor-registration', component:InstructorSignupComponent},
    {path: 'login',component:ConnexionComponent},

    {
        path: 'participant',
        component: ParticipantLayoutComponent,
        children: [
          { path: 'home-page',component:ParticipantHomePageComponent,canActivate: [participantGuard]},
          { path: 'profile-picture', component:ProfilePictureComponent,canActivate: [participantGuard] },
          { path: "edit-profile", component:EditParticipantProfileComponent,canActivate:[participantGuard]},
          { path: 'all-formation', component:ParticipantAllFormationComponent,canActivate:[participantGuard]},
          { path: 'chat', component:ChatPageComponent,canActivate:[participantGuard]},
          { path: 'formation-details', component:ParticipantFormationDetailsComponent,canActivate:[participantGuard]},
          { path: 'course-details/:id', component:ParticipantCourseDetailsComponent,canActivate:[participantGuard]},
          { path: 'instructors', component:InstructorsListComponent,canActivate:[participantGuard]},          
          { path: 'instructors/:id', component:InstructorDetailsPageComponent,canActivate:[participantGuard]},          
        ]
    },

    {
        path : 'instructor', component : InstructorLayoutComponent, 
        children: [
          {path: 'formation-details', component:InstructorFormationDetailsComponent,canActivate:[instructorGuard]},
          {path: 'all-formation', component:AllFormationsComponent,canActivate:[instructorGuard]},
          {path: 'course-details/:id', component:InstructorCourseDetailsComponent,canActivate:[instructorGuard]},
          {path: 'profile-picture', component:InstructorEditProfilePictureComponent,canActivate: [instructorGuard] },
          {path: 'edit-profile', component:EditInstructorProfileComponent,canActivate: [instructorGuard] },
          {path: 'home-page', component:InstructorHomePageComponent,canActivate: [instructorGuard] },
          {path: 'add-formation', component:AddFormationComponent,canActivate: [instructorGuard] },
          {path: 'add-courses', component:AddCoursesComponent,canActivate: [instructorGuard] },
          {path: 'requests', component:ApprovalRequestsComponent,canActivate: [instructorGuard] },
          {path: 'articles', component:InstructorArticlesComponent,canActivate: [instructorGuard] },

        ]

    },
    {path: '', redirectTo: 'home' , pathMatch: 'full'}
];
