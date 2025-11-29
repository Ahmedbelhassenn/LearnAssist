package com.example.LearnAssist.Controllers;
import com.example.LearnAssist.Services.InstructorServices;
import com.example.LearnAssist.Services.ParticipantServices;
import com.example.LearnAssist.ServicesImplementations.ProfilePictureServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfilePictureController {
    @Autowired
    ProfilePictureServicesImpl profilePictureServices;

    @Autowired
    InstructorServices instructorServices;

    @Autowired
    ParticipantServices participantServices;

    String UPLOAD_DIR = "uploads/profile-pictures/";


    @PostMapping("/upload")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, Principal principal,
                                                   @RequestParam("role") String role) {
        try {
            String fileName = profilePictureServices.saveProfilePicture(file);
            Long userId= 0L;
            if ( role.equals("ROLE_INSTRUCTOR")) {
                userId= instructorServices.getInstructorIdFromPrincipal(principal);
            }
            else {
                userId= participantServices.getParticipantIdFromPrincipal(principal);
            }
            profilePictureServices.saveProfilePictureName(fileName,userId,role);
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file");
        }
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
