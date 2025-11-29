import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface ChatRequest {
  question: string;
}

export interface ChatResponse {
  answer: string;
}

@Injectable({
  providedIn: 'root'
})
export class ChatBotService {
  private apiUrl = environment.apiUrl + '/chats';
  private history: { content: string; sender: 'user' | 'bot' }[] = [];

  constructor(private http: HttpClient) {}

  sendMessage(question: string, sessionId: number): Observable<any> {
    const payload = {
      question,
      sessionId,
    };
    return this.http.post<any>(`${this.apiUrl}`, payload);
  }

  getHistory() {
    return this.history;
  }

  clearHistory() {
    this.history = [];
  }
  

 
}

