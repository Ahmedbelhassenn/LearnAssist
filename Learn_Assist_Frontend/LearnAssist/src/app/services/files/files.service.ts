import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FilesService {
  private baseUrl=environment.apiUrl + '/files';
  constructor(private http: HttpClient) { }

  getFormationImage( fileName: string):Observable<Blob>{
      return this.http.get(this.baseUrl+ `/image/${fileName}`, { responseType: 'blob' });
  }
  getChapterVideo(fileName: string):Observable<Blob>{
    return this.http.get(this.baseUrl+ `/video/${fileName}`, { responseType: 'blob' });
  }
  getChapterDocument(fileName: string):Observable<Blob>{
    return this.http.get(this.baseUrl+ `/document/${fileName}`, { responseType: 'blob' });
  }
  getArticleImage(fileName: string):Observable<Blob>{
    return this.http.get(this.baseUrl+ `/article-image/${fileName}`, { responseType: 'blob' });
  }
}
