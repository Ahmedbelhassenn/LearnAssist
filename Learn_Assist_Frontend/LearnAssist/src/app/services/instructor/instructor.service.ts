import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Instructor } from '../../models/instructor.model';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InstructorService {

  private baseUrl= environment.apiUrl + '/instructors';
  constructor(private http: HttpClient) { }

  getInstructorInformation():Observable<any>{
    return this.http.get(this.baseUrl+"/information")
  }

  ModifyInstructorProfile(instructorData:Instructor, oldPassword:String): Observable<any>{
    return this.http.patch(this.baseUrl+`/${oldPassword}`,instructorData)
  }

  ModifyInstructorBio(instructorData:Instructor): Observable<any>{
    return this.http.put(this.baseUrl,instructorData)
  }

  getInstructorList(): Observable<any>{
    return this.http.get(this.baseUrl+'/list')
  }

  getInstructorDetails(id: string): Observable<any>{
    return this.http.get(this.baseUrl+`/details/${id}`)
  }
}
