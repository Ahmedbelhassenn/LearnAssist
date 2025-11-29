import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Formation } from '../../models/formation.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FormationService {

  private baseUrl= environment.apiUrl + '/formations';
  constructor(private http: HttpClient) { }

  getCourses():Observable<any>{
    return this.http.get(this.baseUrl+'/card')
  }

  getInstructorCourses():Observable<any>{
    return this.http.get(this.baseUrl+"/instructor/card")
  }

  addFormation(formData:FormData):Observable<any>{
    return this.http.post(this.baseUrl, formData)
  }

  deleteFormation( id: String):Observable<any>{
    return this.http.delete(this.baseUrl+`/${id}`)
  }

  updateFormation(id: String, formdata:FormData ):Observable<any>{
    return this.http.put(this.baseUrl+`/${id}`, formdata)
  }

  updateFormationStatus(id: String, status: String  ):Observable<any>{
    return this.http.patch(this.baseUrl+`/${id}`, status)
  }

  getFormationById(id: string ):Observable<any>{
    return this.http.get(this.baseUrl+`/${id}`)
  }

  getFormationByInstructorEmail(email: string ):Observable<any>{
    return this.http.get(this.baseUrl+`/participant/${email}/card`)
  }
 
  getFormationsTotalActive(): Observable<any>{
    return this.http.get(this.baseUrl + "/total/active")
  }
}
