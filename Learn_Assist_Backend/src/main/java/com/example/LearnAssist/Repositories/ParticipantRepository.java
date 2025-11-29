package com.example.LearnAssist.Repositories;


import com.example.LearnAssist.Models.Participant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByEmail(String email);
    boolean existsByEmail(String email);
}
