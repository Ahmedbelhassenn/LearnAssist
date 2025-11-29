import { Component, ElementRef, ViewChild, OnInit, HostListener, ViewEncapsulation } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatSessionService } from '../../../services/chat-sessions/chat-session.service';
import { ChatBotService } from '../../../services/chat-bot/chat-bot.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

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
    private sanitizer: DomSanitizer
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

markdownToHtml(content: string): SafeHtml {
  if (!content) return '';

  // D'abord traiter les blocs de code multiligne pour les neutraliser
  let html = content.replace(/```[\s\S]*?```/g, match => {
    // Enlever les backticks et garder seulement le contenu
    const codeContent = match.replace(/```[\w]*\n?|\n```/g, '');
    return `<pre class="bg-gray-100 p-3 rounded mb-4 overflow-x-auto text-sm"><code class="code-block">${codeContent}</code></pre>`;
  });

  // Puis traiter les autres éléments Markdown
  html = html
    // Titres
    .replace(/^### (.*$)/gm, '<h3 class="text-lg font-semibold mb-2">$1</h3>')
    .replace(/^## (.*$)/gm, '<h2 class="text-xl font-bold mb-3 mt-4">$1</h2>')
    .replace(/^# (.*$)/gm, '<h1 class="text-2xl font-bold mb-4 mt-6">$1</h1>')

    // Code inline (simple mise en forme sans coloration)
    .replace(/`([^`]+)`/g, '<code class="bg-gray-100 text-sm font-mono px-1 py-0.5 rounded">$1</code>')

    // Listes
    .replace(/^\* (.*$)/gm, '<li class="list-disc ml-6">$1</li>')
    .replace(/(<li[\s\S]*?<\/li>)/g, '<ul class="space-y-1 mb-2">$1</ul>')

    // Gras
    .replace(/\*\*(.*?)\*\*/g, '<strong class="font-semibold">$1</strong>')

    // Italique
    .replace(/\*(.*?)\*/g, '<em class="italic">$1</em>')

    // Liens
    .replace(/\[(.*?)\]\((.*?)\)/g, '<a href="$2" class="text-indigo-600 hover:underline" target="_blank" rel="noopener">$1</a>')

    // Paragraphes
    .replace(/\n{2,}/g, '</p><p class="mb-4">')
    .replace(/^(?!<h\d|<ul|<li|<p|<code|<strong|<em|<a|<\/?pre)(.+)$/gm, '<p class="mb-4">$1</p>');

  return this.sanitizer.bypassSecurityTrustHtml(
    `<div class="prose prose-sm max-w-full break-words">${html}</div>`
  );
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
