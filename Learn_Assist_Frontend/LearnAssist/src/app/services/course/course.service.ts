import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CourseService {

  private baseUrl= environment.apiUrl + '/courses';
    constructor(private http: HttpClient) { }

  addCourseToFormation(idFormation: string, formData: FormData):Observable<any>{
    return this.http.post(this.baseUrl+`/${idFormation}`,formData)
  }

  editCourse(idCourse: string, courseData: {title: string, description: string}):Observable<any>{
    return this.http.patch(this.baseUrl+`/${idCourse}`,courseData)
  }

  getCourses(idFormation: string):Observable<any>{
    return this.http.get(this.baseUrl+`/formation/${idFormation}`)
  }

  getCourseById(id: string): Observable<any> {
    return this.http.get(`${this.baseUrl}/${id}`);
  }
  

  deleteCourse(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`)
  }


}
