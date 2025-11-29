import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatSessionService {

  private baseUrl=environment.apiUrl + '/sessions';
  constructor(private http: HttpClient) { }

  getParticipantSessions(): Observable<any>{
    return this.http.get(this.baseUrl+'/list')
  }

  createSession(message: String): Observable<any>{
    return this.http.post(this.baseUrl,message)
  }

  getSessionMessages(id: number): Observable<any>{
    return this.http.get(this.baseUrl+`/${id}`);
  }

  deleteSession(id: number): Observable<any>{
    return this.http.delete(this.baseUrl+`/${id}`);
  }

  updateSessionTitle(id: number, question: string): Observable<any>{
    return this.http.put(this.baseUrl+`/${id}`, question);
  }
}
