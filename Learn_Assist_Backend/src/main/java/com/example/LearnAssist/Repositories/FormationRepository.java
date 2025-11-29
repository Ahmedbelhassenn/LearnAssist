package com.example.LearnAssist.Repositories;


import com.example.LearnAssist.Models.Formation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FormationRepository extends JpaRepository<Formation, Long> {
    Formation findByTitle(String title);
    boolean existsByTitle(String title);
}
