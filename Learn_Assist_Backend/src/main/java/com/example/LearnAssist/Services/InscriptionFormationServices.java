package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.InscriptionFormation;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

public interface InscriptionFormationServices {
    InscriptionFormation getInscriptionFormationById(Long id);
    List<InscriptionFormation> getAllInscriptionFormationByParticipantId(Long id);
    List<HashMap<String,String>> getAllInscriptionFormationByInstructor(Principal principal);
    void addInscriptionFormation(InscriptionFormation inscriptionFormation);
    void updateInscriptionFormation(InscriptionFormation inscriptionFormation);
    void inscriptionDemand( Long formationId, String newName,
                           String newPhone, String newEmail, Principal principal);
    HashMap<String,String> getInscriptionDemandById(Long id);
    void deleteInscriptionFormation(Long id, Principal principal);
    HashMap<String,String> getInscriptionDemandByFormationAndParticipantId(Long id, Principal principal);
    void approveInscriptionFormation(Long id, Principal principal);
    void rejectInscriptionFormation(Long id, Principal principal);
    boolean isParticipantApproved(Long formationId, Principal principal);
    Long getTotalParticipantsByInstructor(Principal principal);

}
