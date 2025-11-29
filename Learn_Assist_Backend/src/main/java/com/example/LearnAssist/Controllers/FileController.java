package com.example.LearnAssist.Controllers;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final Path videoStoragePath = Paths.get("uploads/videos/");
    private final Path documentStoragePath = Paths.get("uploads/documents/");
    private final Path imageStoragePath = Paths.get("uploads/courses-pictures/");
    private final Path articleImageStoragePath = Paths.get("uploads/article-pictures/");

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR', 'PARTICIPANT')")
    @GetMapping("/video/{fileName}")
    public ResponseEntity<Resource> getVideo(@PathVariable String fileName) throws IOException {
        return serveFile(videoStoragePath, fileName, "video/mp4");
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR', 'PARTICIPANT')")
    @GetMapping("/document/{fileName}")
    public ResponseEntity<Resource> getDocument(@PathVariable String fileName) throws IOException {
        return serveFile(documentStoragePath, fileName, "application/pdf");
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        try {
            Path filePath = imageStoragePath.resolve(fileName).normalize();
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

    @GetMapping("/article-image/{fileName}")
    public ResponseEntity<Resource> getArticleImage(@PathVariable String fileName) {
        try {
            Path filePath = articleImageStoragePath.resolve(fileName).normalize();
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


    private ResponseEntity<Resource> serveFile(Path baseDir, String fileName, String mediaType) throws IOException {
        Path filePath = baseDir.resolve(fileName).normalize();
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new UrlResource(filePath.toUri());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }
}