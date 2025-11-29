package com.example.LearnAssist.Repositories;

import com.example.LearnAssist.Models.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    boolean existsByTitle(String title);
}
