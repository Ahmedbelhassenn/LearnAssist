package com.example.LearnAssist.Services;


import com.example.LearnAssist.Models.Participant;

import java.security.Principal;
import java.util.List;
import java.util.Map;

public interface ParticipantServices {
    Participant getParticipantById(Long id);
    Map<String,String> getParticipantInformationById(Long id);
    List<Participant> getAllParticipants();
    Participant addParticipant(Participant participant);
    void updateParticipant(Participant participant, Long id);
    void deleteParticipant(Long id);
    Long getParticipantIdFromPrincipal(Principal principal);
}
