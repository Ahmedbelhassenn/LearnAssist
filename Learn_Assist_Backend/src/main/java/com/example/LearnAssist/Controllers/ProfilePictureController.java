package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Exceptions.InvalidFileException;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.ServicesImplementations.ProfilePictureServicesImpl;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profiles")
public class ProfilePictureController {
    @Autowired
    ProfilePictureServicesImpl profilePictureServices;

    @Autowired
    FileStorageService fileStorageService;

    /**
     * The "role" request parameter is still accepted for backward compatibility with the
     * frontend, but ignored: the account type comes from the authenticated user.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file,
                                                  Authentication authentication) {
        try {
            String fileName = profilePictureServices.updateProfilePicture(authentication, file);
            Map<String, String> response = new HashMap<>();
            response.put("fileName", fileName);
            return ResponseEntity.ok(response);
        } catch (InvalidFileException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading file");
        }
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getProfilePicture(@PathVariable String fileName) {
        return FileController.serve(fileStorageService, FileCategory.PROFILE_PICTURE, fileName);
    }
}
