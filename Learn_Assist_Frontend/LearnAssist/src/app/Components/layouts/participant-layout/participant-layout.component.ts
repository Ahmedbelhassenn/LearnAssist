import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ChatBotComponent } from "../../Participant-Pages/chat-bot/chat-bot.component";
import { ParticipantNavbarComponent } from "../../Participant-Pages/participant-navbar/participant-navbar.component";
import { filter } from 'rxjs';
import { NgClass, NgIf } from '@angular/common';

@Component({
  selector: 'app-participant-layout',
  standalone: true,
  imports: [RouterOutlet, ParticipantNavbarComponent, ChatBotComponent, NgIf],
  templateUrl: './participant-layout.component.html',
  styleUrl: './participant-layout.component.css'
})
export class ParticipantLayoutComponent {

  showChatBot: boolean = true;
  constructor(private router: Router) {
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: any) => {
      // Cacher le chatbot sur la route "/chat"
      this.showChatBot = !event.url.includes('/chat');
    });
  }

}
