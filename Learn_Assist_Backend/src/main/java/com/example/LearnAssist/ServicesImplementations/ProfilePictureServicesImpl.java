package com.example.LearnAssist.ServicesImplementations;


import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@Service
public class ProfilePictureServicesImpl {
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    InstructorRepository instructorRepository;
    private static final String UPLOAD_DIR = "uploads/profile-pictures/";
    private static final String UPLOAD_PIC = "uploads/courses-pictures/";
    private static final String UPLOAD_Article = "uploads/article-pictures/";
    public String saveImage(MultipartFile file) throws IOException {
        // Vérifier si le dossier existe, sinon le créer
        File uploadDir = new File(UPLOAD_PIC);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        // Générer un nom unique pour éviter les conflits
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_PIC, fileName);
        // Sauvegarder le fichier sur le serveur
        Files.write(filePath, file.getBytes());

        return fileName;
    }
    public String saveArticleImage(MultipartFile file) throws IOException {
        File uploadDir = new File(UPLOAD_Article);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_Article, fileName);
        Files.write(filePath, file.getBytes());
        return fileName;
    }

    public String saveProfilePicture(MultipartFile file) throws IOException {
        // Vérifier si le dossier existe, sinon le créer
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        // Générer un nom unique pour éviter les conflits
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR, fileName);
        // Sauvegarder le fichier sur le serveur
        Files.write(filePath, file.getBytes());

        return fileName;
    }
    public void saveProfilePictureName(String photoName, Long id, String role) throws IOException {
        if(Objects.equals(role, "ROLE_INSTRUCTOR")) {
            Instructor instructor = instructorRepository.findById(id).orElseThrow(
                    () -> new ExceptionError("Instructor Not Found")
            );
            instructor.setProfilePhoto(photoName);
            instructorRepository.save(instructor);
        }
        else if(Objects.equals(role, "ROLE_PARTICIPANT")) {
            Participant participant=participantRepository.findById(id).orElseThrow(
                    () -> new ExceptionError("Participant not found")
            );
            participant.setProfilePhoto(photoName);
            participantRepository.save(participant);
        }

    }
}
