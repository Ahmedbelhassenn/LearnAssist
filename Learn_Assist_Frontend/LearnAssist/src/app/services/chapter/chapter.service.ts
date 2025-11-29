import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChapterService {
 private baseUrl= environment.apiUrl + '/chapters';
  constructor(private http: HttpClient) { }

  addChapterToCourse(idCourse: string, formData: FormData):Observable<any>{
    return this.http.post(this.baseUrl+`/${idCourse}`,formData)
  }

  deleteChapter(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`)
  }

  getChapterById(id: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/${id}`);
  }

  editChapter(idChapter: string, chapterData: FormData):Observable<any>{
    return this.http.patch(this.baseUrl+`/${idChapter}`,chapterData)
  }

  getInstructorTotalVideos(): Observable<any>{
    return this.http.get(this.baseUrl+ '/videos/total')
  }

  getInstructorTotalResources(): Observable<any>{
    return this.http.get(this.baseUrl+ '/resources/total')
  }
}
