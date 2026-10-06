package com.example.LearnAssist.Repositories;

import com.example.LearnAssist.Models.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Every lookup used by the API is scoped to the owning participant, so a session of
 * another participant can never be loaded by id alone.
 */
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    Optional<ChatSession> findByIdAndParticipantEmail(Long id, String participantEmail);

    List<ChatSession> findByParticipantEmailOrderByCreatedAtDesc(String participantEmail);

    boolean existsByTitle(String title);

}
