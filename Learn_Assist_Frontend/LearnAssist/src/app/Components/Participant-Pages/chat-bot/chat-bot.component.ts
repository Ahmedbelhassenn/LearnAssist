import { CommonModule, NgClass, NgFor, NgIf } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatBotService } from '../../../services/chat-bot/chat-bot.service';
import { MarkdownService } from '../../../services/markdown/markdown.service';
import { ChatSessionService } from '../../../services/chat-sessions/chat-session.service';


interface Message{
  role: string;
  content: string;
  timestamp: Date
}

@Component({
  selector: 'app-chat-bot',
  standalone: true,
  imports: [FormsModule, NgIf, NgFor, CommonModule],
  templateUrl: './chat-bot.component.html',
  styleUrl: './chat-bot.component.css'
})
export class ChatBotComponent implements AfterViewChecked {
  constructor(private chatBotService: ChatBotService,  private markdown: MarkdownService , private sessionService: ChatSessionService){}
  @ViewChild('chatContainer') private chatContainer!: ElementRef;
  
  
  messages: Message[]= [
    {  role: 'assistant', content: 'Bonjour ! Je suis votre assistant LearnAssist. Comment puis-je vous aider aujourd\'hui ?', timestamp: new Date() }
  ];
  
  userInput = '';
  loading = false;
  isOpen = false;
  userScrolledUp = false;
  sessionId: number | null = null;
  sidebarOpen = false; 


  isLargeScreen = window.innerWidth >= 1024;
  showDeleteModal= false;
  private lockScroll = false;

  ngAfterViewChecked(): void {
    if (!this.userScrolledUp) {
      this.scrollToBottom();
    }
  }
  onScroll(): void {
    const element = this.chatContainer.nativeElement;
    const threshold = 10;
    const position = element.scrollTop + element.clientHeight;
    const height = element.scrollHeight;

    this.userScrolledUp = (position + threshold) < height;
  }

  scrollToBottom() {
    const el = this.chatContainer?.nativeElement;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }
  
  toggleChat() {
    this.isOpen = !this.isOpen;
  }

  send() {
    this.loading=true;
    const question = this.userInput.trim();
    if (!question) return;
  
  
    // Ajout du message utilisateur immédiatement
    this.messages.push({  role: 'user', content: question, timestamp: new Date() });
    if (!this.sessionId) {
      // 🟢 Création d'une nouvelle session + réponse en même temps
      this.sessionService.createSession(question).subscribe({
  
        next: (response: any) => {
          this.messages.push({ role: 'assistant', content: '',  timestamp: new Date()});
          this.displayAssistantMessage(response.response);
          this.sessionId=response.sessionId
          this.loading=false;
          this.scrollToBottom()
        },
        error: (err) => {
          console.error('Erreur lors de la création de la session :', err);
          this.loading=false;
          this.scrollToBottom()
  
        }
      });
    } else {
      // 🟡 Session existante : envoi simple
      this.chatBotService.sendMessage(question, this.sessionId).subscribe({
        next: (response: any) => {
          this.messages.push({ role: 'assistant', content: '', timestamp: new Date() });
          this.displayAssistantMessage(response.response);
                  this.loading=false;
          this.scrollToBottom()
  
        },
        error: (err) => {
          this.showError(err)
          this.loading=false;
          this.scrollToBottom()
  
        }
      });
    }
    this.userInput='';
  
  }

  showError(msg: string) {
    this.messages.push({
      role: 'assistant',
      content: msg,
      timestamp: new Date(),
    });
  }

  displayAssistantMessage(message: string) {
    const words = message.split(' ');
    let currentMessage = '';
    let index = 0;
  
    const interval = setInterval(() => {
      if (index < words.length) {
        currentMessage += (index > 0 ? ' ' : '') + words[index];
        this.updateLastAssistantMessage(currentMessage);
        index++;
        if (index % 5 === 0 || index === words.length - 1) {
          this.scrollToBottom();
        }
        
      } else {
        clearInterval(interval);
      }
    }, 80); // vitesse d'affichage (ms)
  }

  updateLastAssistantMessage(content: string) {
    const lastMessage = this.messages[this.messages.length - 1];
    if (lastMessage && lastMessage.role === 'assistant') {
      lastMessage.content = content;
    }
  }
  

  /** Renders Markdown safely (marked + DOMPurify, no Angular sanitizer bypass). */
  markdownToHtml(content: string): string {
    return this.markdown.render(content);
  }

  
}