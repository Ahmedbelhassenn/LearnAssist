package com.example.LearnAssist.Repositories;

import com.example.LearnAssist.Models.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    boolean existsByTitle(String title);
}
