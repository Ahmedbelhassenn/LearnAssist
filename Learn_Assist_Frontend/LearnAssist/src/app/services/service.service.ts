import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ServiceService {

  private baseUrl= environment.apiUrl;


  constructor(private http: HttpClient) {};

  capitalizeFirstLetter(text: string): string {
    if (!text) return '';
    return text.charAt(0).toUpperCase() + text.slice(1);
  }
  

  uploadProfilePhoto(formData:FormData):Observable<any>{
    return this.http.post(this.baseUrl+'/profiles/upload', formData);
  }
  
  getProfilePicture(fileName: string): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/profiles/image/${fileName}`, { responseType: 'blob' });
  }
  
}


