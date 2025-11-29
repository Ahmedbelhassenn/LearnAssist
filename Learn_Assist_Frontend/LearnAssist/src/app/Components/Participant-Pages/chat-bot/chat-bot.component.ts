import { CommonModule, NgClass, NgFor, NgIf } from '@angular/common';
import { AfterViewChecked, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChatBotService } from '../../../services/chat-bot/chat-bot.service';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
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
  styleUrl: './chat-bot.component.scss'
})
export class ChatBotComponent implements AfterViewChecked {
  constructor(private chatBotService: ChatBotService,  private sanitizer: DomSanitizer , private sessionService: ChatSessionService){}
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

  
}