package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Dto.CreateFormationRequest;
import com.example.LearnAssist.Dto.UpdateFormationRequest;
import com.example.LearnAssist.Models.Chapter;
import com.example.LearnAssist.Models.Course;
import com.example.LearnAssist.Models.Formation;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Repositories.FormationRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Services.FormationServices;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class FormationServicesImpl implements FormationServices {
    static final String DRAFT_STATUS = "draft";

    @Autowired
    private FormationRepository formationRepository;
    @Autowired
    private InstructorRepository instructorRepository;
    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public List<Formation> getAllFormations() {
        return formationRepository.findAll();
    }

    /**
     * Always creates a NEW formation: the id, owner, rate and status are decided here,
     * never by the client (the DTO does not even carry them).
     */
    @Override
    public Long addFormation(CreateFormationRequest request, MultipartFile image, MultipartFile video, String email) {
        if (!instructorRepository.existsByEmail(email)) {
            throw new ExceptionError("Vous n'avez pas le droit d'ajouter cette formation");
        }
        if (formationRepository.existsByTitle(request.getTitle())) {
            throw new ExceptionError("Formation with this title already exists");
        }
        Formation formation = new Formation();
        formation.setTitle(request.getTitle());
        formation.setDescription(request.getDescription());
        formation.setVideoUrl(blankToNull(request.getVideoUrl()));
        formation.setFormationCategory(request.getFormationCategory());
        formation.setFormationLanguage(request.getFormationLanguage());
        formation.setPrice(request.getPrice());
        formation.setFormationDuration(request.getFormationDuration());
        formation.setFormationLevel(request.getFormationLevel());
        formation.setCertification(request.getCertification());
        formation.setFormationCreationDate(LocalDate.now().toString());
        formation.setFormationStatus(DRAFT_STATUS);
        formation.setEmailInstructor(email);

        List<String[]> storedFiles = new ArrayList<>();
        try {
            formation.setImageFileName(storeTracked(image, FileCategory.FORMATION_IMAGE, storedFiles));
            formation.setVideoFileName(storeTracked(video, FileCategory.VIDEO, storedFiles));
            return formationRepository.save(formation).getId();
        } catch (RuntimeException e) {
            deleteTracked(storedFiles);
            throw e;
        }
    }

    @Override
    public void updateFormation(Long id, UpdateFormationRequest request, MultipartFile image, MultipartFile video, String email) {
        Formation existingFormation = formationRepository.findById(id).orElseThrow(
                () -> new ExceptionError("Formation not found")
        );
        // Ownership is checked BEFORE any file is written.
        if (!email.equals(existingFormation.getEmailInstructor())) {
            throw new ExceptionError("Vous n'avez pas le droit de modifier cette formation");
        }
        if (!request.getTitle().equals(existingFormation.getTitle())
                && formationRepository.existsByTitle(request.getTitle())) {
            throw new ExceptionError("Formation with this title already exists");
        }
        existingFormation.setTitle(request.getTitle());
        if (request.getDescription() != null) {
            existingFormation.setDescription(request.getDescription());
        }
        if (request.getFormationCategory() != null) {
            existingFormation.setFormationCategory(request.getFormationCategory());
        }
        if (request.getFormationLanguage() != null) {
            existingFormation.setFormationLanguage(request.getFormationLanguage());
        }
        if (request.getFormationDuration() != null) {
            existingFormation.setFormationDuration(request.getFormationDuration());
        }
        if (request.getFormationLevel() != null) {
            existingFormation.setFormationLevel(request.getFormationLevel());
        }
        if (request.getCertification() != null) {
            existingFormation.setCertification(request.getCertification());
        }
        if (request.getPrice() != null) {
            existingFormation.setPrice(request.getPrice());
        }
        if (request.getVideoUrl() != null) {
            existingFormation.setVideoUrl(blankToNull(request.getVideoUrl()));
        }

        String previousImage = existingFormation.getImageFileName();
        String previousVideo = existingFormation.getVideoFileName();
        List<String[]> storedFiles = new ArrayList<>();
        try {
            String newImage = storeTracked(image, FileCategory.FORMATION_IMAGE, storedFiles);
            String newVideo = storeTracked(video, FileCategory.VIDEO, storedFiles);
            if (newImage != null) {
                existingFormation.setImageFileName(newImage);
            }
            if (newVideo != null) {
                existingFormation.setVideoFileName(newVideo);
            }
            formationRepository.save(existingFormation);
            // Old files are only removed once the new ones are referenced in the database.
            if (newImage != null) {
                fileStorageService.delete(FileCategory.FORMATION_IMAGE, previousImage);
            }
            if (newVideo != null) {
                fileStorageService.delete(FileCategory.VIDEO, previousVideo);
            }
        } catch (RuntimeException e) {
            deleteTracked(storedFiles);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteFormation(Long id, String email) {
        Formation formation = formationRepository.findById(id).orElseThrow(
                () -> new ExceptionError("Formation with id " + id + " not found"));
        if (!email.equals(formation.getEmailInstructor())) {
            throw new ExceptionError("Vous n'avez pas le droit de supprimer cette formation");
        }
        // Collect every file of the formation (courses and chapters are deleted by cascade).
        fileStorageService.delete(FileCategory.FORMATION_IMAGE, formation.getImageFileName());
        fileStorageService.delete(FileCategory.VIDEO, formation.getVideoFileName());
        if (formation.getCourses() != null) {
            for (Course course : formation.getCourses()) {
                if (course.getChapters() == null) {
                    continue;
                }
                for (Chapter chapter : course.getChapters()) {
                    fileStorageService.delete(FileCategory.VIDEO, chapter.getVideoFileName());
                    fileStorageService.delete(FileCategory.DOCUMENT, chapter.getDocumentFileName());
                }
            }
        }
        // Inside the transaction, the file deletions above only run after a successful commit.
        formationRepository.delete(formation);
    }

    private String storeTracked(MultipartFile file, FileCategory category, List<String[]> storedFiles) {
        String fileName = fileStorageService.storeIfPresent(file, category);
        if (fileName != null) {
            storedFiles.add(new String[]{category.name(), fileName});
        }
        return fileName;
    }

    private void deleteTracked(List<String[]> storedFiles) {
        for (String[] stored : storedFiles) {
            fileStorageService.delete(FileCategory.valueOf(stored[0]), stored[1]);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
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
