package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Exceptions.ResourceNotFoundException;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePictureServicesImpl {
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    InstructorRepository instructorRepository;
    @Autowired
    FileStorageService fileStorageService;

    /**
     * Replaces the profile picture of the authenticated user. The account type is taken
     * from the authentication, never from the request. The user is resolved BEFORE the
     * file is written, and the previous picture is deleted once the new one is saved.
     */
    @Transactional
    public String updateProfilePicture(Authentication authentication, MultipartFile file) {
        String email = authentication.getName();
        if (hasRole(authentication, "ROLE_INSTRUCTOR")) {
            Instructor instructor = instructorRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Instructor not found"));
            String fileName = fileStorageService.store(file, FileCategory.PROFILE_PICTURE);
            String previous = instructor.getProfilePhoto();
            instructor.setProfilePhoto(fileName);
            instructorRepository.save(instructor);
            fileStorageService.delete(FileCategory.PROFILE_PICTURE, previous);
            return fileName;
        }
        if (hasRole(authentication, "ROLE_PARTICIPANT")) {
            Participant participant = participantRepository.findByEmail(email).orElseThrow(
                    () -> new ResourceNotFoundException("Participant not found"));
            String fileName = fileStorageService.store(file, FileCategory.PROFILE_PICTURE);
            String previous = participant.getProfilePhoto();
            participant.setProfilePhoto(fileName);
            participantRepository.save(participant);
            fileStorageService.delete(FileCategory.PROFILE_PICTURE, previous);
            return fileName;
        }
        throw new ResourceNotFoundException("User not found");
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
