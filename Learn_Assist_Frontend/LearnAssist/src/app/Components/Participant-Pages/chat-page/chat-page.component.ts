import { Component, ElementRef, ViewChild, OnInit, HostListener, ViewEncapsulation } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatSessionService } from '../../../services/chat-sessions/chat-session.service';
import { ChatBotService } from '../../../services/chat-bot/chat-bot.service';
import { MarkdownService } from '../../../services/markdown/markdown.service';

interface Message{
  role: string;
  content: string;
  timestamp: Date
}

interface Session{
  id: number;
  title: string;
  preview: string;
  date: Date
}

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [NgFor, CommonModule, FormsModule ],
  templateUrl: './chat-page.component.html',
  styleUrls: ['./chat-page.component.css']
})
export class ChatPageComponent implements OnInit {


  @ViewChild('chatContainer') private chatContainer!: ElementRef;
  @ViewChild('messageInput') messageInput!: ElementRef;

  userInput = '';
  loading = false;
  userScrolledUp = false;
  activeSessionId: number | null = null;
  sidebarOpen = false; 
  idSession: number | null = null

  
  sessions: Session[]= [];
  isLargeScreen = window.innerWidth >= 1024;
  showDeleteModal= false;

@HostListener('window:resize', ['$event'])
onResize(event: any) {
  this.isLargeScreen = event.target.innerWidth >= 1024;
  
}
  
  messages: Message[]= [
    {  role: 'assistant', content: 'Bonjour ! Je suis votre assistant LearnAssist. Comment puis-je vous aider aujourd\'hui ?', timestamp: new Date() }
  ];
  

  
  constructor (private sessionService: ChatSessionService, private chatBotService: ChatBotService,
    private markdown: MarkdownService
  ) {
  }

  ngOnInit(): void {
      this.getSessions();
  }

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

  scrollToBottom(): void {
    const element = this.chatContainer.nativeElement;
    element.scrollTop = element.scrollHeight;
  }

send() {
  this.loading=true;
  const question = this.userInput.trim();
  if (!question) return;


  // Ajout du message utilisateur immédiatement
  this.messages.push({ role: 'user', content: question, timestamp: new Date() });
  if (!this.activeSessionId) {
    // 🟢 Création d'une nouvelle session + réponse en même temps
    this.sessionService.createSession(question).subscribe({

      next: (response: any) => {
        this.activeSessionId = response.sessionId; // récupération de l'id
        this.getSessions(); 
        this.messages.push({ role: 'assistant', content: '',  timestamp: new Date()});
        this.displayAssistantMessage(response.response);
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
    this.chatBotService.sendMessage(question, this.activeSessionId).subscribe({
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

/** Renders Markdown safely (marked + DOMPurify, no Angular sanitizer bypass). */
markdownToHtml(content: string): string {
  return this.markdown.render(content);
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

  
  showError(msg: string) {
    this.messages.push({
      role: 'assistant',
      content: msg,
      timestamp: new Date(),
    });
  }


  sendQuickSuggestion(suggestion: string) {
    this.userInput = suggestion;
    
  }


  toggleSidebar() {
    this.sidebarOpen = !this.sidebarOpen;
  }


  getSessions(): void{

    this.sessionService.getParticipantSessions().subscribe({
      next: (response)=>{
        this.sessions=response.sessions
      },

      error: (error)=>{
        console.log(error)
      }
    })

  }

  getSessionMessages(){
    if(this.activeSessionId){
      this.sessionService.getSessionMessages(this.activeSessionId).subscribe({
        next: (response)=> {
          this.messages=response.chatSession;
        },
        error: (error)=>{
          console.log(error)
        }
      })
    }
  }

  goToSession(id: number) {
    this.activeSessionId = id;
    this.toggleSidebar();
    this.getSessionMessages();
  }

  handleEnter(event: any) {
  const keyboardEvent = event as KeyboardEvent;
  if (!keyboardEvent.shiftKey) {
    keyboardEvent.preventDefault();
    this.send();
  }
}
  
goToNewSession(){
  this.activeSessionId=null;
  this.messages=[
    {  role: 'assistant', content: 'Bonjour ! Je suis votre assistant LearnAssist. Comment puis-je vous aider aujourd\'hui ?', timestamp: new Date() }
  ];
  this.toggleSidebar()
}

deleteSession(id: number) {
  this.idSession=id
  this.showDeleteModal=true
  
}

openedMenuId: number | null = null;
editingSessionId: number | null = null;

toggleMenu(sessionId: number, event: Event): void {
  event.stopPropagation();
  this.openedMenuId = this.openedMenuId === sessionId ? null : sessionId;
}

startEditing(sessionId: number, event: Event): void {
  event.stopPropagation();
  this.openedMenuId = null;
  this.editingSessionId = sessionId;
}

updateSessionTitle(session: Session): void {
  
  if(this.activeSessionId){
    this.sessionService.updateSessionTitle(session.id, session.title).subscribe(() => {
      this.editingSessionId = null;
    });}
}

confirmDeleteSession() {
  const id=this.idSession
  if(id){
    this.sessionService.deleteSession(id).subscribe({
      next: () => {
        this.sessions = this.sessions.filter(s => s.id !== id);
        if (this.activeSessionId === id) {
          this.goToNewSession(); // reset session affichée
        }
        console.log("deleted successfully")
      },
      error: (err) => {
        console.error('Erreur lors de la suppression de la session :', err);
      }
    });
  }
  this.showDeleteModal=false
    
 
  }


  
  
}
