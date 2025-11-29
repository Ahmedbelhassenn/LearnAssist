package com.example.LearnAssist.ServicesImplementations;


import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import com.example.LearnAssist.Services.InstructorServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class InstructorServicesImpl implements InstructorServices {
    @Autowired
    InstructorRepository instructorRepository;
    @Autowired
    PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    ParticipantRepository participantRepository;

    public Instructor getInstructorById(Long id){
        return instructorRepository.findById(id).orElseThrow(()-> new RuntimeException("Could not find instructor with id: " + id));
    }
    public List<Instructor> getAllInstructors(){
        return instructorRepository.findAll();
    }

    public void updateInstructor(Instructor instructor, Long id) {
        Instructor instructor1 = instructorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Could not find instructor with id: " + id));
        if (instructor.getFirstName() != null && !instructor.getFirstName().isEmpty()) {
            instructor1.setFirstName(instructor.getFirstName());
        }
        if (instructor.getLastName() != null && !instructor.getLastName().isEmpty()) {
            instructor1.setLastName(instructor.getLastName());
        }
        if (instructor.getEmail() != null && !instructor.getEmail().isEmpty()) {
            instructor1.setEmail(instructor.getEmail());
        }
        if (instructor.getBio() != null && !instructor.getBio().isEmpty()) {
            instructor1.setBio(instructor.getBio());
        }
        if (instructor.getDateOfBirth() != null ) {
            instructor1.setDateOfBirth(instructor.getDateOfBirth());
        }
        if (instructor.getCity() != null && !instructor.getCity().isEmpty()) {
            instructor1.setCity(instructor.getCity());
        }
        if (instructor.getGender() != null && !instructor.getGender().isEmpty()) {
            instructor1.setGender(instructor.getGender());
        }
        if (instructor.getPhone() != null && !instructor.getPhone().isEmpty()) {
            instructor1.setPhone(instructor.getPhone());
        }
        if (instructor.getSpeciality() != null && !instructor.getSpeciality().isEmpty()) {
            instructor1.setSpeciality(instructor.getSpeciality());
        }
        if (instructor.getStatus() != null && !instructor.getStatus().isEmpty()) {
            instructor1.setStatus(instructor.getStatus());
        }
        if (instructor.getProfilePhoto() != null && !instructor.getProfilePhoto().isEmpty()) {
            instructor1.setProfilePhoto(instructor.getProfilePhoto());
        }
        instructorRepository.save(instructor1);
    }

    public Map<String,String> getInstructorInformationById(Long id) {
        Instructor instructor=instructorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Instructor not found")
        );
        Map<String,String> map=new HashMap<>();
        map.put("email",instructor.getEmail());
        map.put("firstName",instructor.getFirstName());
        map.put("lastName",instructor.getLastName());
        map.put("bio",instructor.getBio());
        map.put("profilePhotoUrl",instructor.getProfilePhoto());
        map.put("dateOfBirth",instructor.getDateOfBirth().toString());
        map.put("city",instructor.getCity());
        map.put("gender",instructor.getGender());
        map.put("phone",instructor.getPhone());
        map.put("speciality",instructor.getSpeciality());
        map.put("status",instructor.getStatus());
        return map;
    }

    @Override
    public Long getInstructorIdFromPrincipal(Principal principal) {
        String name = principal.getName();
        return instructorRepository.findByEmail(name)
                .map(Instructor :: getId)
                .orElseThrow(()-> new RuntimeException("Could not find instructor with email: " + name));
    }

    @Override
    public List<HashMap<String,String>> getInstructorsWithDetails() {
        List<Instructor> instructors=instructorRepository.findAll();
        List<HashMap<String,String>> list=new ArrayList<>();
        for(Instructor instructor:instructors){
            HashMap<String,String> map=new HashMap<>();
            map.put("id",instructor.getId().toString());
            map.put("firstName",instructor.getFirstName());
            map.put("lastName",instructor.getLastName());
            map.put("gender",instructor.getGender());
            map.put("email",instructor.getEmail());
            map.put("bio",instructor.getBio());
            map.put("speciality",instructor.getSpeciality());
            map.put("photoUrl",instructor.getProfilePhoto());
            list.add(map);
        }
        return list;

    }

    @Override
    public HashMap<String, String> getInstructorWithDetails(Long id) {
        Instructor instructor= instructorRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Could not find instructor with this id " )
        );
        HashMap<String, String> map=new HashMap<>();
        map.put("firstName",instructor.getFirstName());
        map.put("lastName",instructor.getLastName());
        map.put("gender",instructor.getGender());
        map.put("email",instructor.getEmail());
        map.put("bio",instructor.getBio());
        map.put("photoUrl",instructor.getProfilePhoto());
        map.put("dateOfBirth",instructor.getDateOfBirth().toString());
        map.put("city",instructor.getCity());
        map.put("phone",instructor.getPhone());
        map.put("speciality",instructor.getSpeciality());
        return map;
    }

    public void deleteInstructor(Long id){
        instructorRepository.deleteById(id);
    }

    public Instructor addInstructor(Instructor instructor) {
        if(participantRepository.existsByEmail(instructor.getEmail())
                || instructorRepository.existsByEmail(instructor.getEmail())){
            throw new ExceptionError("Email already registered!");
        }
        instructor.setPassword(passwordEncoder.encode(instructor.getPassword()));
        instructor.setRole("INSTRUCTOR");
        return instructorRepository.save(instructor);
    }

}
