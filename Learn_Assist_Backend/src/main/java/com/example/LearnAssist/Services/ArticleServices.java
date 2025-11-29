package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.Article;

import java.security.Principal;
import java.util.List;

public interface ArticleServices {
    Article getArticle(Long id);
    List<Article> getAllArticles();
    void addArticle(Article article);
    List<Article> getInstructorArticles(String email);
    void editArticle(Long id, Article article, String email);
    void deleteArticle(Long id, String email);

}
