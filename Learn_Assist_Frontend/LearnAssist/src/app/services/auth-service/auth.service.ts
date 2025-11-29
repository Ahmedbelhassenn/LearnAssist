import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Participant } from '../../models/participant.model';
import { Instructor } from '../../models/instructor.model';
import { jwtDecode } from 'jwt-decode';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private baseUrl= environment.apiUrl;

  constructor(private http: HttpClient) {}
  
  loginParticipant(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<{ token: string }>(this.baseUrl + '/participant/login', credentials);
  }
  registerParticipant(participant: Participant): Observable<any>{
    return this.http.post(this.baseUrl + '/participant/register', participant);
  }
  loginInstructor(credentials: { email: string; password: string }): Observable<any> {
    return this.http.post<{ token: string }>(this.baseUrl + '/instructor/login', credentials);
  }
  registerInstructor(instructor:Instructor): Observable<any>{
    return this.http.post(this.baseUrl+'/instructor/register',instructor)
  }

  
  isTokenExpired(token: string): boolean {
    try {
      const decodedToken: any = jwtDecode(token);
      const expirationDate = decodedToken.exp * 1000; // Convertir en millisecondes
      return Date.now() > expirationDate;
    } catch (error) {
      return true; // Si une erreur se produit, considérer le token comme invalide
    }
  }

}
