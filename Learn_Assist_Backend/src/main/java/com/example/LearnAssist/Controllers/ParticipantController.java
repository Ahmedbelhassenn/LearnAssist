package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Services.ParticipantServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping("/api/participants")
public class ParticipantController {
    @Autowired
    ParticipantServices participantServices;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public List<Participant> getAllParticipants() {
        return participantServices.getAllParticipants();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Participant getParticipantById(@PathVariable Long id) {
        return participantServices.getParticipantById(id);
    }

    @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN')")
    @PutMapping("/{oldPassword}")
    public ResponseEntity<?> updateParticipant( @PathVariable String oldPassword,Principal principal,
                                               @RequestBody Participant participant) {
        try{
            Long id=participantServices.getParticipantIdFromPrincipal(principal);
            Participant updatedParticipant = participantServices.getParticipantById(id);
            HashMap<String,String> hashMap = new HashMap<>();
            if(!passwordEncoder.matches(oldPassword, updatedParticipant.getPassword())) {
                hashMap.put("error", "Wrong old password");
                return new ResponseEntity<>(hashMap, HttpStatus.BAD_REQUEST);
            }
            participantServices.updateParticipant(participant,id);
            hashMap.put("success", "Participant successfully updated");
            return new ResponseEntity<>(hashMap, HttpStatus.OK);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }


    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public void deleteParticipant(@PathVariable Long id) {
        participantServices.deleteParticipant(id);
    }



    @PreAuthorize("hasAnyRole('PARTICIPANT','ADMIN')")
    @GetMapping ("information")
    public ResponseEntity<?> getParticipantInformation(Principal principal) {
        try {
            Long userId = participantServices.getParticipantIdFromPrincipal(principal);
            participantServices.getParticipantById(userId);
            return ResponseEntity.ok().body(participantServices.getParticipantInformationById(userId));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
