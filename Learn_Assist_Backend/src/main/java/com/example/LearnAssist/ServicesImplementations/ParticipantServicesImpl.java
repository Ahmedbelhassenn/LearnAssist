package com.example.LearnAssist.ServicesImplementations;


import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.RegisterParticipantRequest;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import com.example.LearnAssist.Services.ParticipantServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParticipantServicesImpl implements ParticipantServices {
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    InstructorRepository instructorRepository;
    @Autowired
    PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
    @Autowired
    EmailAvailabilityChecker emailAvailabilityChecker;

    public Participant getParticipantById(Long id){
        return participantRepository.findById(id).orElseThrow(()-> new RuntimeException("Participant not found"));
    }

    public Map<String,String> getParticipantInformationById(Long id) {
        Participant participant=participantRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Participant not found")
        );
        Map<String,String> map=new HashMap<>();
        map.put("email",participant.getEmail());
        map.put("firstName",participant.getFirstName());
        map.put("lastName",participant.getLastName());
        map.put("profilePhotoUrl",participant.getProfilePhoto());
        map.put("phone",participant.getPhone());
        map.put("gender",participant.getGender());
        map.put("educationLevel",participant.getEducationLevel());
        map.put("city",participant.getCity());
        map.put("dateOfBirth",participant.getDateOfBirth().toString());
        return map;
    }

    public List<Participant> getAllParticipants(){
        return participantRepository.findAll();
    }

    /**
     * Always creates a NEW participant from the whitelisted DTO fields. The id is generated
     * by the database, the role is forced and status/profile photo are never client-controlled.
     */
    public Participant registerParticipant(RegisterParticipantRequest request){
        String email = request.getEmail().trim();
        if(emailAvailabilityChecker.isTaken(email)){
            throw new ExceptionError("Email already registered!");
        }
        Participant participant = new Participant();
        participant.setFirstName(request.getFirstName().trim());
        participant.setLastName(request.getLastName().trim());
        participant.setPhone(request.getPhone().trim());
        participant.setEmail(email);
        participant.setGender(request.getGender());
        participant.setDateOfBirth(request.getDateOfBirth());
        participant.setCity(request.getCity().trim());
        participant.setEducationLevel(request.getEducationLevel());
        participant.setPassword(passwordEncoder.encode(request.getPassword()));
        participant.setRole("PARTICIPANT");
        return participantRepository.save(participant);
    }

    public void updateParticipant(Participant participant, Long id){
        Participant participant1=participantRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Participant not found")
        );
        if(participant.getEmail()!=null && !participant.getEmail().isEmpty()){
            participant1.setEmail(participant.getEmail());
        }
        if(participant.getFirstName()!=null && !participant.getFirstName().isEmpty()){
            participant1.setFirstName(participant.getFirstName());
        }
        if(participant.getLastName()!=null && !participant.getLastName().isEmpty()){
            participant1.setLastName(participant.getLastName());
        }
        if(participant.getProfilePhoto()!=null && !participant.getProfilePhoto().isEmpty()){
            participant1.setProfilePhoto(participant.getProfilePhoto());
        }
        if(participant.getPhone()!=null && !participant.getPhone().isEmpty()){
            participant1.setPhone(participant.getPhone());
        }
        if(participant.getGender()!=null && !participant.getGender().isEmpty()){
            participant1.setGender(participant.getGender());
        }
        if(participant.getEducationLevel()!=null && !participant.getEducationLevel().isEmpty()){
            participant1.setEducationLevel(participant.getEducationLevel());
        }
        if(participant.getCity()!=null && !participant.getCity().isEmpty()){
            participant1.setCity(participant.getCity());
        }
        if(participant.getDateOfBirth()!=null){
            participant1.setDateOfBirth(participant.getDateOfBirth());
        }
        if(participant.getStatus()!=null && !participant.getStatus().isEmpty()){
            participant1.setStatus(participant.getStatus());
        }
        if(participant.getPassword()!=null && !participant.getPassword().isEmpty()){
            participant1.setPassword(passwordEncoder.encode(participant.getPassword()));
        }
        participantRepository.save(participant1);
    }

    public Long getParticipantIdFromPrincipal(Principal principal) {
        String email = principal.getName();
        return participantRepository.findByEmail(email)
                .map(Participant::getId)
                .orElseThrow(() -> new ExceptionError("Participant non trouvé pour l'email : " + email));
    }

    public void deleteParticipant(Long id){
        participantRepository.deleteById(id);
    }
}
