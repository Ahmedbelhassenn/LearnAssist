import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class InscriptionFormationService {

  baseUrl= environment.apiUrl + '/inscription-formations';

  constructor(private http: HttpClient) { }

  addInscriptionDemand(inscriptionFormationData:{participantName: string, participantEmail: string, participantPhone: string,formationId: string}): Observable<any>{
    return this.http.post(this.baseUrl +'/demand',inscriptionFormationData)
  }

  deleteInscriptionDemand(id : string): Observable<any>{
    return this.http.delete(this.baseUrl+`/${id}`)
  }

  getInscriptionFormationStatus(formationId: String): Observable<any>{
    return this.http.get(this.baseUrl+`/formation-id-${formationId}`)
  }

  getAllInscriptionFormationByInstructor(): Observable<any>{
    return this.http.get(this.baseUrl+`/demand`)
  }

  onApprove(formationId: String) : Observable<any>{
    return this.http.patch(this.baseUrl+`/demand/${formationId}`, "Approved")
  }

  onReject(formationId: String) : Observable<any>{
    return this.http.delete(this.baseUrl+`/demand/${formationId}`)
  }

  getTotalParticipants(): Observable<any>{
    return this.http.get(this.baseUrl+"/total-participants")
  }
  
}
