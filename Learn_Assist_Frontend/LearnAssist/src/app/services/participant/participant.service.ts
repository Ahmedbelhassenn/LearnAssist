import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Participant } from '../../models/participant.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ParticipantService {
  private baseUrl= environment.apiUrl + '/participants';

  constructor(private http: HttpClient) { }

  getParticipantInformation():Observable<any>{
    return this.http.get(this.baseUrl+"/information")
  }

  ModifyParticipantProfile(participantData:Participant, oldPassword: string): Observable<any>{
    return this.http.put(this.baseUrl+`/${oldPassword}`,participantData)
  }
}
