package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.Article;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

public interface ArticleServices {
    Article getArticle(Long id);
    List<Article> getAllArticles();
    void addArticle(Article article, MultipartFile image);
    List<Article> getInstructorArticles(String email);
    void editArticle(Long id, Article article, MultipartFile image, String email);
    void deleteArticle(Long id, String email);

}
