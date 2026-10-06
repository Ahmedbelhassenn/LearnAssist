package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Article;
import com.example.LearnAssist.Repositories.ArticleRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.ArticleServices;
import com.example.LearnAssist.Services.FileStorageService;
import com.example.LearnAssist.Storage.FileCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServicesImpl implements ArticleServices {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private InstructorRepository instructorRepository;

    @Override
    public Article getArticle(Long id) {
        return articleRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Article with id " + id + " not found")
        );
    }
    @Override
    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }
    @Override
    public void addArticle(Article article, MultipartFile image) {
        if (articleRepository.existsByTitle(article.getTitle())) {
            throw new ExceptionError("Article with this title already exists");
        }
        String imageFileName = fileStorageService.storeIfPresent(image, FileCategory.ARTICLE_IMAGE);
        try {
            article.setImageFileName(imageFileName);
            articleRepository.save(article);
        } catch (RuntimeException e) {
            fileStorageService.delete(FileCategory.ARTICLE_IMAGE, imageFileName);
            throw e;
        }
    }

    @Override
    public List<Article> getInstructorArticles(String email) {
        List<Article> allArticles = articleRepository.findAll();
        List<Article> instructorArticles = new ArrayList<>();
        for (Article article : allArticles) {
            if (article.getInstructor().getEmail().equals(email)) {
                instructorArticles.add(article);
            }
        }
        return instructorArticles;

    }

    @Override
    public void editArticle(Long id, Article article, MultipartFile image, String email) {
        Article articleToEdit = articleRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Article with not found")
        );
        if (!articleToEdit.getInstructor().getEmail().equals(email)) {
            throw new ExceptionError("You are not allowed to edit this article");
        }
        if (!articleToEdit.getTitle().equals(article.getTitle())) {
            if (articleRepository.existsByTitle(article.getTitle())) {
                throw new ExceptionError("Article with this title already exists");
            }
            articleToEdit.setTitle(article.getTitle());
        }
        if (article.getContent()!= null && !article.getContent().isEmpty()){
            articleToEdit.setContent(article.getContent());
        }
        if (article.getPublishedAt()!=null ){
            articleToEdit.setPublishedAt(article.getPublishedAt());
        }
        // Ownership was checked above: only now is the new image written.
        String previousImage = articleToEdit.getImageFileName();
        String newImage = fileStorageService.storeIfPresent(image, FileCategory.ARTICLE_IMAGE);
        try {
            if (newImage != null) {
                articleToEdit.setImageFileName(newImage);
            }
            articleRepository.save(articleToEdit);
        } catch (RuntimeException e) {
            fileStorageService.delete(FileCategory.ARTICLE_IMAGE, newImage);
            throw e;
        }
        if (newImage != null) {
            fileStorageService.delete(FileCategory.ARTICLE_IMAGE, previousImage);
        }
    }

    @Override
    public void deleteArticle(Long id, String email) {
        Article existingArticle=articleRepository.findById(id).orElseThrow(
                () -> new RuntimeException("Article not found")
        );
        String InstructorEmail = existingArticle.getInstructor().getEmail();
        if (!email.equals(InstructorEmail)) {
            throw new ExceptionError("You are not allowed to delete this Article");
        }
        articleRepository.deleteById(id);
        fileStorageService.delete(FileCategory.ARTICLE_IMAGE, existingArticle.getImageFileName());
    }

}
