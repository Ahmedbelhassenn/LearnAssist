package com.example.LearnAssist.Controllers;


import com.example.LearnAssist.Dto.InscriptionFormationRequest;

import com.example.LearnAssist.Services.InscriptionFormationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;

@RestController
@RequestMapping("/api/inscription-formations")
public class InscriptionFormationController {
    @Autowired
    InscriptionFormationServices inscriptionFormationServices;

    @PreAuthorize("hasAnyRole('PARTICIPANT', 'ADMIN')")
    @PostMapping("/demand")
    public ResponseEntity<?> addDemand(@RequestBody InscriptionFormationRequest inscriptionFormation,
                                    Principal principal) {

        try {
            inscriptionFormationServices.inscriptionDemand(inscriptionFormation.getFormationId(),
                    inscriptionFormation.getParticipantName(),inscriptionFormation.getParticipantPhone(),
                    inscriptionFormation.getParticipantEmail(), principal);
            HashMap<String, String> response = new HashMap<>();
            response.put("message", "Demand successful");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            HashMap<String,String> response = new HashMap<>();
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasRole('PARTICIPANT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDemand(@PathVariable Long id, Principal principal) {

        HashMap<String,String> response = new HashMap<>();
        try{
            inscriptionFormationServices.deleteInscriptionFormation(id, principal);
            response.put("message","Successfully deleted");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/formation-id-{formationId}")
    public ResponseEntity<?> getDemandById(@PathVariable Long formationId, Principal principal) {
        HashMap<String,String> response = new HashMap<>();
        try {
            response=inscriptionFormationServices.getInscriptionDemandByFormationAndParticipantId(formationId, principal);
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/demand")
    public ResponseEntity<?> getAllInscriptionFormationByInstructor(Principal principal) {
        HashMap<String,Object> response = new HashMap<>();
        try {
            response.put("list", inscriptionFormationServices.getAllInscriptionFormationByInstructor(principal));
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @PatchMapping("/demand/{id}")
    public ResponseEntity<?> approveInscriptionFormation(Principal principal, @PathVariable Long id) {
        HashMap<String,String> response = new HashMap<>();
        try {
            inscriptionFormationServices.approveInscriptionFormation(id, principal);
            response.put("message","Successfully approved");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @DeleteMapping("/demand/{id}")
    public ResponseEntity<?> rejectInscriptionFormation(Principal principal, @PathVariable Long id) {
        HashMap<String,String> response = new HashMap<>();
        try {
            inscriptionFormationServices.rejectInscriptionFormation(id, principal);
            response.put("message","Successfully rejected");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/total-participants")
    public ResponseEntity<?> getTotalParticipants(Principal principal) {
        HashMap<String,Object> response = new HashMap<>();
        try {
            response.put("total", inscriptionFormationServices.getTotalParticipantsByInstructor(principal));
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            response.put("message",e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

}
