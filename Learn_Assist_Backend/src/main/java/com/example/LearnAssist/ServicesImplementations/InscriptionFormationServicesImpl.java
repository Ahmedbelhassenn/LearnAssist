package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Models.InscriptionFormation;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.FormationRepository;
import com.example.LearnAssist.Repositories.InscriptionFormationRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import com.example.LearnAssist.Services.InscriptionFormationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class InscriptionFormationServicesImpl implements InscriptionFormationServices {

    @Autowired
    InscriptionFormationRepository inscriptionFormationRepository;

    @Autowired
    FormationRepository formationRepository;

    @Autowired
    ParticipantRepository participantRepository;

    @Override
    public InscriptionFormation getInscriptionFormationById(Long id) {
        return inscriptionFormationRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Could not find inscriptionFormation")
        );
    }

    @Override
    public List<InscriptionFormation> getAllInscriptionFormationByParticipantId(Long id) {
        List<InscriptionFormation> inscriptionFormations= inscriptionFormationRepository.findAll();
        for (InscriptionFormation inscriptionFormation : inscriptionFormations) {
            if (inscriptionFormation.getParticipant().getId().equals(id)) {
                inscriptionFormations.add(inscriptionFormation);
            }
        }
        return inscriptionFormations;
    }

    @Override
    public List<HashMap<String,String>> getAllInscriptionFormationByInstructor(Principal principal) {
        String email = principal.getName();
        List<InscriptionFormation> inscriptionFormations = inscriptionFormationRepository.findAll();
        List<HashMap<String,String>> inscriptionFormationHashMap = new ArrayList<>();
        for (InscriptionFormation inscriptionFormation : inscriptionFormations) {
            if (inscriptionFormation.getFormation().getEmailInstructor().equals(email) ){
                HashMap<String,String> inscriptionFormationHashMapMap = new HashMap<>();
                inscriptionFormationHashMapMap.put("id", inscriptionFormation.getId().toString());
                inscriptionFormationHashMapMap.put("createdAt", inscriptionFormation.getCreatedAt().toString());
                inscriptionFormationHashMapMap.put("participantName", inscriptionFormation.getUpdatedName());
                inscriptionFormationHashMapMap.put("participantEmail", inscriptionFormation.getUpdatedEmail());
                inscriptionFormationHashMapMap.put("formationTitle", inscriptionFormation.getFormation().getTitle());
                inscriptionFormationHashMapMap.put("participantPhone", inscriptionFormation.getUpdatedPhone());
                inscriptionFormationHashMapMap.put("participantEmailAccount", inscriptionFormation.getParticipant().getEmail());
                inscriptionFormationHashMapMap.put("status", inscriptionFormation.getStatus());

                inscriptionFormationHashMap.add(inscriptionFormationHashMapMap);
            }
        }
        return inscriptionFormationHashMap;
    }

    @Override
    public void addInscriptionFormation(InscriptionFormation inscriptionFormation) {

        inscriptionFormationRepository.save(inscriptionFormation);

    }

    @Override
    public void updateInscriptionFormation(InscriptionFormation inscriptionFormation) {
        inscriptionFormationRepository.save(inscriptionFormation);
    }

    @Override
    public void inscriptionDemand(  Long formationId,String newName, String newPhone,
                                  String newEmail, Principal principal) {
        Formation formation= formationRepository.findById(formationId).orElseThrow(
                () -> new ExceptionError("Could not find formation")
        );
        String email = principal.getName();

        Participant participant = participantRepository.findByEmail(email).orElseThrow(
                () -> new ExceptionError("Could not find participant")
        );

        InscriptionFormation inscriptionFormation = new InscriptionFormation();
        inscriptionFormation.setFormation(formation);
        inscriptionFormation.setParticipant(participant);
        inscriptionFormation.setStatus("Pending");
        inscriptionFormation.setUpdatedEmail(newEmail);
        inscriptionFormation.setUpdatedName(newName);
        inscriptionFormation.setUpdatedPhone(newPhone);
        inscriptionFormation.setCreatedAt(LocalDateTime.now());

        inscriptionFormationRepository.save(inscriptionFormation);

    }

    @Override
    public HashMap<String, String> getInscriptionDemandById(Long id) {
        InscriptionFormation inscriptionFormation = inscriptionFormationRepository.findById(id).orElseThrow(
                () -> new ExceptionError("Could not find inscriptionFormation")
        );
        HashMap<String, String> inscriptionDemand = new HashMap<>();
        inscriptionDemand.put("name", inscriptionFormation.getUpdatedName());
        inscriptionDemand.put("phone", inscriptionFormation.getUpdatedPhone());
        inscriptionDemand.put("email", inscriptionFormation.getUpdatedEmail());
        inscriptionDemand.put("participantEmailAccount", inscriptionFormation.getParticipant().getEmail());
        inscriptionDemand.put("formationTitle", inscriptionFormation.getFormation().getTitle());
        inscriptionDemand.put("createdAt", inscriptionFormation.getCreatedAt().toString());
        return inscriptionDemand;

    }

    @Override
    public void deleteInscriptionFormation(Long id, Principal principal) {
        InscriptionFormation inscriptionFormation = inscriptionFormationRepository
                .findById(id).orElseThrow( () -> new ExceptionError("Could not find inscriptionFormation"));
        String email = principal.getName();
        if (inscriptionFormation.getParticipant().getEmail().equals(email)) {
            inscriptionFormationRepository.delete(inscriptionFormation);
        }
        else {
            throw new ExceptionError("You are not allowed to delete this inscription formation.");
        }
    }

    @Override
    public HashMap<String, String> getInscriptionDemandByFormationAndParticipantId(Long id, Principal principal) {
        List<InscriptionFormation> inscriptionFormations = inscriptionFormationRepository.findAll();
        String email = principal.getName();
        HashMap<String, String> inscriptionDemand = new HashMap<>();
        for (InscriptionFormation inscriptionFormation : inscriptionFormations) {
            if (inscriptionFormation.getFormation().getId().equals(id) && inscriptionFormation.getParticipant().getEmail().equals(email)) {
                inscriptionDemand.put("id" , inscriptionFormation.getId().toString());
                inscriptionDemand.put("status" , inscriptionFormation.getStatus());
            }
        }
        return inscriptionDemand;

    }

    @Override
    public void approveInscriptionFormation(Long id, Principal principal) {
        InscriptionFormation inscriptionFormation=inscriptionFormationRepository.findById(id).orElseThrow(
                () -> new ExceptionError("Could not find inscriptionFormation")
        );
        String email = principal.getName();
        if (inscriptionFormation.getFormation().getEmailInstructor().equals(email)) {
            inscriptionFormation.setStatus("Approved");
        }
        inscriptionFormationRepository.save(inscriptionFormation);
    }

    @Override
    public void rejectInscriptionFormation(Long id, Principal principal) {
        String email = principal.getName();
        InscriptionFormation inscriptionFormation = inscriptionFormationRepository.findById(id).orElseThrow(
                () -> new ExceptionError("Could not find inscriptionFormation")
        );
        if (inscriptionFormation.getFormation().getEmailInstructor().equals(email)) {
            inscriptionFormationRepository.delete(inscriptionFormation);
        }
    }

    @Override
    public boolean isParticipantApproved(Long formationId, Principal principal) {
        Formation formation=formationRepository.findById(formationId).orElseThrow(
                () -> new ExceptionError("Could not find formation")
        );
        String email = principal.getName();
        Participant participant= participantRepository.findByEmail(email).orElseThrow(
                () -> new ExceptionError("Could not find participant")
        );
        InscriptionFormation inscriptionFormation= inscriptionFormationRepository
                .findInscriptionFormationByFormationAndParticipant(formation, participant).orElseThrow(
                        () -> new ExceptionError("Could not find inscription formation")
                );
        return inscriptionFormation.getStatus().equals("Approved");
    }

    @Override
    public Long getTotalParticipantsByInstructor(Principal principal) {
        List<InscriptionFormation> inscriptionFormations = inscriptionFormationRepository.findAll();
        Long totalParticipants = 0L;
        String email = principal.getName();
        for (InscriptionFormation inscriptionFormation : inscriptionFormations) {
            if (inscriptionFormation.getFormation().getEmailInstructor().equals(email) && inscriptionFormation.getStatus().equals("Approved")) {
                totalParticipants ++ ;
            }
        }
        return totalParticipants;
    }
}