import { DatePipe, NgClass, NgFor, NgIf } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { InscriptionFormationService } from '../../../services/inscriptionFormation/inscription-formation.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-approval-requests',
  standalone: true,
  imports: [NgIf, NgFor, NgClass, DatePipe],
  templateUrl: './approval-requests.component.html',
  styleUrl: './approval-requests.component.css'
})
export class ApprovalRequestsComponent implements OnInit {

  requests: any[] = [];

  constructor(private inscriptionFormationService: InscriptionFormationService){}

  
  ngOnInit(): void {
      this.fetchRequests();
  }


   fetchRequests() {
    this.inscriptionFormationService.getAllInscriptionFormationByInstructor().subscribe({
      next: (data) => (this.requests = data.list),
      error: (err) => console.error('Erreur lors du chargement des demandes', err),
    });
   }

  approveRequest(id: string) {
    this.inscriptionFormationService.onApprove(id).subscribe({
      next: () => this.fetchRequests(),
      error: (err) => console.error('Erreur lors de l\'approbation', err),
    });
  }

  rejectRequest(id: string) {
    this.inscriptionFormationService.onReject(id).subscribe({
      next: () => this.fetchRequests(),
      error: (err) => console.error('Erreur lors du rejet', err),
    });
  }

  confirmAction() {
    const requestId = this.confirmationModal.data.id;
    debugger

    if (this.confirmationModal.type === 'approve') {
      this.approveRequest(requestId);
    } else {
      this.rejectRequest(requestId);
    }

    this.confirmationModal.isOpen = false;
    setTimeout( ()=> {
      this.fetchRequests();
    }, 500
    )
    
  }
  openConfirmationModal(request: any, type: 'approve' | 'reject') {
    this.confirmationModal = {
      isOpen: true,
      type,
      data: request
    };
  }
  confirmationModal = {
  isOpen: false,
  type: '', // 'approve' ou 'reject'
  data: null as any, // la demande sélectionnée
  };
  cancelConfirmation() {
    this.confirmationModal.isOpen = false;
  }
}
