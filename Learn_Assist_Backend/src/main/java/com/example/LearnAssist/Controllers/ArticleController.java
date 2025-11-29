package com.example.LearnAssist.Controllers;


import com.example.LearnAssist.Dto.ArticleDto;
import com.example.LearnAssist.Models.Article;
import com.example.LearnAssist.Models.Instructor;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.ArticleServices;
import com.example.LearnAssist.ServicesImplementations.ProfilePictureServicesImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    @Autowired
    ArticleServices articleServices;
    @Autowired
    ProfilePictureServicesImpl profilePictureServices;
    @Autowired
    InstructorRepository InstructorRepository;
    @Autowired
    private InstructorRepository instructorRepository;


    @GetMapping()
    public ResponseEntity<?> getAllArticles() {
        try {
            List<Article> articles = articleServices.getAllArticles();
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            HashMap <String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<?> getArticle(@PathVariable Long id) {
        HashMap <String, Object> response = new HashMap<>();
        try {
            Article article =articleServices.getArticle(id);
            return ResponseEntity.ok(article);
        }
        catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/instructor")
    public ResponseEntity<?> getInstructorArticles(Principal principal) {
        HashMap <String, Object> response = new HashMap<>();
        try{
            String email = principal.getName();
            List<Article> articles = articleServices.getInstructorArticles(email);
            return ResponseEntity.ok(articles);
        }
        catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/instructor/{id}")
    public ResponseEntity<?> getInstructorArticles(@PathVariable Long id) {
        HashMap <String, Object> response = new HashMap<>();
        try{

            String email = instructorRepository.findById(id).get().getEmail();
            List<Article> articles = articleServices.getInstructorArticles(email);
            return ResponseEntity.ok(articles);
        }
        catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addArticle(@RequestPart ArticleDto article , Principal principal, @RequestParam(value = "image", required = false) MultipartFile image) {
        HashMap <String, Object> response = new HashMap<>();
        try {
            Article newArticle = new Article();
            newArticle.setTitle(article.getTitle());
            newArticle.setContent(article.getContent());
            newArticle.setPublishedAt(article.getPublishedAt());
            if (image != null && !image.isEmpty()) {
                String imagePath = profilePictureServices.saveArticleImage(image);
                newArticle.setImageFileName(imagePath);
            }
            String email = principal.getName();
            Instructor instructor = instructorRepository.findByEmail(email).orElseThrow(
                    () -> new RuntimeException("instructor not found")
            );
            newArticle.setInstructor(instructor);
            articleServices.addArticle(newArticle);
            response.put("message", "Article added successfully");
            return ResponseEntity.ok(response);
        }catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }

    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable Long id , Principal principal) {
        HashMap <String, Object> response = new HashMap<>();
        try {
            articleServices.deleteArticle(id,principal.getName() );
            response.put("message", "Article deleted successfully");
            return ResponseEntity.ok(response);
        }
        catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateArticle(@PathVariable Long id,@RequestPart ArticleDto article , Principal principal,@RequestParam(value = "image", required = false) MultipartFile image ) {
        HashMap <String, Object> response = new HashMap<>();
        try {
            Article newArticle = new Article();
            newArticle.setTitle(article.getTitle());
            newArticle.setContent(article.getContent());
            newArticle.setPublishedAt(article.getPublishedAt());
            if (image != null && !image.isEmpty()) {
                String imagePath = profilePictureServices.saveArticleImage(image);
                newArticle.setImageFileName(imagePath);
            }
            String email = principal.getName();
            articleServices.editArticle(id, newArticle, email);
            response.put("message", "Article updated successfully");
            return ResponseEntity.ok(response);

        }catch (Exception e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
