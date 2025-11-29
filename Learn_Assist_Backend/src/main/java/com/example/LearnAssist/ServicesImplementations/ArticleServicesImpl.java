package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Configurations.ExceptionError;
import com.example.LearnAssist.Models.Article;
import com.example.LearnAssist.Repositories.ArticleRepository;
import com.example.LearnAssist.Repositories.InstructorRepository;
import com.example.LearnAssist.Services.ArticleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ArticleServicesImpl implements ArticleServices {

    @Autowired
    private ArticleRepository articleRepository;
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
    public void addArticle(Article article) {
        if (articleRepository.existsByTitle(article.getTitle())) {
            throw new ExceptionError("Article with this title already exists");
        }
        articleRepository.save(article);
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
    public void editArticle(Long id, Article article, String email) {
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
        if (article.getImageFileName() != null && !article.getImageFileName().isEmpty()){
            articleToEdit.setImageFileName(article.getImageFileName());
        }

        articleRepository.save(articleToEdit);
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
    }

}
