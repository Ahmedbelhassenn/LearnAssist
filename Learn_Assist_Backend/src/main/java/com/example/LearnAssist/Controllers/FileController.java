package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR', 'PARTICIPANT')")
    @GetMapping("/video/{fileName}")
    public ResponseEntity<Resource> getVideo(@PathVariable String fileName) {
        return serve(fileStorageService, FileCategory.VIDEO, fileName);
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR', 'PARTICIPANT')")
    @GetMapping("/document/{fileName}")
    public ResponseEntity<Resource> getDocument(@PathVariable String fileName) {
        return serve(fileStorageService, FileCategory.DOCUMENT, fileName);
    }

    @GetMapping("/image/{fileName}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName) {
        return serve(fileStorageService, FileCategory.FORMATION_IMAGE, fileName);
    }

    @GetMapping("/article-image/{fileName}")
    public ResponseEntity<Resource> getArticleImage(@PathVariable String fileName) {
        return serve(fileStorageService, FileCategory.ARTICLE_IMAGE, fileName);
    }

    /**
     * Serves a stored file. Unsafe names (path separators, traversal) and missing files
     * both answer 404. The content type is derived from the extension.
     */
    static ResponseEntity<Resource> serve(FileStorageService storage, FileCategory category, String fileName) {
        return storage.load(category, fileName)
                .map(file -> ResponseEntity.ok()
                        .contentType(file.mediaType())
                        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                                .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                        .body(file.resource()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
