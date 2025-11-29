package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Services.FileServices;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileServicesImpl  implements FileServices {

    public String saveVideoFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/videos/";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName; // uniquement le nom du fichier, pas le chemin complet
    }
    public String saveDocumentFile(MultipartFile file) throws IOException {
        String uploadDir = "uploads/documents/";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        Files.createDirectories(filePath.getParent());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return fileName; // uniquement le nom du fichier, pas le chemin complet
    }
}
