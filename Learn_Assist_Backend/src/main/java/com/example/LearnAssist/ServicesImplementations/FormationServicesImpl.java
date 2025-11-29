package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Repositories.FormationRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.FormationServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class FormationServicesImpl implements FormationServices {
    @Autowired
    private FormationRepository formationRepository;
    @Autowired
    private InstructorRepository instructorRepository;

    @Override
    public List<Formation> getAllFormations() {
        return formationRepository.findAll();
    }

    @Override
    public Long addFormation(Formation formation, String email) {
        if (formationRepository.existsByTitle(formation.getTitle())) {
            throw new ExceptionError("Formation with this title already exists");
        }
        if(!formation.getEmailInstructor().equals(email)) {
            throw new ExceptionError("Vous n'avez pas le droit d'ajouter cette formation");
        }
        formationRepository.save(formation);
        return formationRepository.findByTitle(formation.getTitle()).getId();
    }

    @Override
    public void updateFormation(Long id, Formation formation, String email ) {
            Formation existingFormation=formationRepository.findById(id).orElseThrow(
                    () -> new RuntimeException("Formation not found")
            );
            if (!existingFormation.getEmailInstructor().equals(email)){
                throw new ExceptionError("Vous n'avez pas le droit de modifier cette formation");
            }
            if(!formation.getTitle().equals(existingFormation.getTitle())) {
                if(formationRepository.existsByTitle(formation.getTitle())) {
                    throw new ExceptionError("Formation with this title already exists");
                }
            }
            existingFormation.setTitle(formation.getTitle());
            if(formation.getDescription()!=null){
                existingFormation.setDescription(formation.getDescription());
            }
            if(formation.getFormationStatus()!=null){
                existingFormation.setFormationStatus(formation.getFormationStatus());
            }
            if(formation.getFormationCategory()!=null){
                existingFormation.setFormationCategory(formation.getFormationCategory());
            }
            if(formation.getFormationLanguage()!=null){
                existingFormation.setFormationLanguage(formation.getFormationLanguage());
            }
            if(formation.getFormationDuration()!=null){
                existingFormation.setFormationDuration(formation.getFormationDuration());
            }
            if(formation.getCertification()!=null){
                existingFormation.setCertification(formation.getCertification());
            }

            if(formation.getPrice()!=null){
                existingFormation.setPrice(formation.getPrice());
            }
            if(formation.getVideoUrl()!=null){
                existingFormation.setVideoUrl(formation.getVideoUrl());
            }
            if(formation.getImageFileName()!=null){
                existingFormation.setImageFileName(formation.getImageFileName());
            }
            if(formation.getVideoFileName()!=null){
                existingFormation.setVideoFileName(formation.getVideoFileName());
            }
            formationRepository.save(existingFormation);
    }

    @Override
    public void deleteFormation(Long id, String email) {
        if (!formationRepository.existsById(id)) {
            throw new ExceptionError("Formation with id " + id + " not found");
        } else if (!formationRepository.getReferenceById(id).getEmailInstructor().equals(email)) {
            throw  new ExceptionError("Vous n'avez pas le droit de supprimer cette formation");
        }
        formationRepository.deleteById(id);
    }

    @Override
    public List<HashMap<String,String>> getFormationsCard() {
        List<Formation> formations = formationRepository.findAll();
        List<HashMap<String, String>> formationsCard = new ArrayList<>();
        for (Formation formation : formations) {
            HashMap<String,String> formationCard = new HashMap<>();
            if(Objects.equals(formation.getFormationStatus(), "published")){
                Instructor instructor=instructorRepository.findByEmail(formation.getEmailInstructor())
                        .orElseThrow(() -> new RuntimeException("Instructor with email " + formation.getEmailInstructor() + " not found"));
                formationCard.put("id", String.valueOf(formation.getId()));
                String instructorName=instructor.getFirstName()+" "+instructor.getLastName();
                formationCard.put("instructorName", instructorName);
                formationCard.put("title", formation.getTitle());
                formationCard.put("imageFileName", formation.getImageFileName());
                formationCard.put("price", formation.getPrice());
                formationCard.put("formationLevel", formation.getFormationLevel());
                formationCard.put("formationDuration", formation.getFormationDuration());
                formationsCard.add(formationCard);
            }

        }
        return formationsCard;
    }

    public List<HashMap<String,String>> getFormationsCardByEmail(String email) {
        List<Formation> formations = formationRepository.findAll();
        List<HashMap<String, String>> formationsCard = new ArrayList<>();
        for (Formation formation : formations) {
            if(formation.getEmailInstructor().equals(email)){
                HashMap<String,String> formationCard = new HashMap<>();
                formationCard.put("title", formation.getTitle());
                formationCard.put("videoFileName", formation.getVideoFileName());
                formationCard.put("price", formation.getPrice());
                formationCard.put("formationLevel", formation.getFormationLevel());
                formationCard.put("formationDuration", formation.getFormationDuration());
                formationCard.put("imageFileName", formation.getImageFileName());
                formationCard.put("id", String.valueOf(formation.getId()));
                formationCard.put("status", formation.getFormationStatus());
                Instructor instructor=instructorRepository.findByEmail(formation.getEmailInstructor())
                        .orElseThrow(() -> new RuntimeException("Instructor with email " + formation.getEmailInstructor() + " not found"));
                String instructorName=instructor.getFirstName()+" "+instructor.getLastName();
                formationCard.put("instructorName", instructorName);
                formationsCard.add(formationCard);
            }

        }
        return formationsCard;
    }

    public void updateFormationStatus(Long id, String status, String email) {

        Formation formation = formationRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Formation not found"));
        if (!formation.getEmailInstructor().equals(email)) {
            throw new ExceptionError("Vous n'avez pas le droit de modifier ce bio");
        }
        formation.setFormationStatus(status);
        formationRepository.save(formation);
    }

    @Override
    public List<HashMap<String, String>> getFormationsCardByInstructorEmail(String email) {
        List<Formation> formations = formationRepository.findAll();
        List<HashMap<String, String>> formationsCard = new ArrayList<>();
        for (Formation formation : formations) {
            if(formation.getEmailInstructor().equals(email)){
                if(Objects.equals(formation.getFormationStatus(), "published")){
                    HashMap<String,String> formationCard = new HashMap<>();
                    formationCard.put("title", formation.getTitle());
                    formationCard.put("price", formation.getPrice());
                    formationCard.put("formationLevel", formation.getFormationLevel());
                    formationCard.put("formationDuration", formation.getFormationDuration());
                    formationCard.put("imageFileName", formation.getImageFileName());
                    formationCard.put("id", String.valueOf(formation.getId()));
                    formationsCard.add(formationCard);
                }
            }
        }
        return formationsCard;
    }

    @Override
    public Integer getInstructorTotalActiveFormation(Principal principal) {
        String email = principal.getName();
        int result = 0;
        List<Formation> formations = formationRepository.findAll();
        for (Formation formation : formations) {
            if(formation.getEmailInstructor().equals(email)){
                if(Objects.equals(formation.getFormationStatus(), "published")){
                    result ++;
                }

            }
        }
        return result;
    }

    @Override
    public Integer getInstructorTotalVideosFormation(Principal principal) {
        String email = principal.getName();
        int result = 0;
        List<Formation> formations = formationRepository.findAll();
        for (Formation formation : formations) {
            if(formation.getEmailInstructor().equals(email)){
                if(formation.getVideoFileName()!= null){
                    result ++;
                }
            }
        }
        return result;
    }

    public HashMap<String,String> getFormationCardById(Long id) {
        Formation formation = formationRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Formation not found")
        );
        HashMap<String,String> formationCard = new HashMap<>();
        formationCard.put("title", formation.getTitle());
        formationCard.put("description", formation.getDescription());
        formationCard.put("videoFileName", formation.getVideoFileName());
        formationCard.put("price", formation.getPrice());
        formationCard.put("formationLevel", formation.getFormationLevel());
        formationCard.put("formationDuration", formation.getFormationDuration());
        formationCard.put("imageFileName", formation.getImageFileName());
        formationCard.put("videoUrl", formation.getVideoUrl());
        formationCard.put("id", String.valueOf(formation.getId()));
        formationCard.put("status", formation.getFormationStatus());
        Instructor instructor=instructorRepository.findByEmail(formation.getEmailInstructor())
                .orElseThrow(() -> new RuntimeException("Instructor with email " + formation.getEmailInstructor() + " not found"));
        String instructorName=instructor.getFirstName()+" "+instructor.getLastName();
        formationCard.put("instructorName", instructorName);
        formationCard.put("instructorBio", instructor.getBio());
        formationCard.put("instructorProfilePhoto",instructor.getProfilePhoto());
        return formationCard;
    }

}
