package com.example.LearnAssist.Repositories;


import com.example.LearnAssist.Models.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByParticipantIdOrderByTimestampAsc(Long userId);
    List<ChatMessage> findByChatSessionIdOrderByTimestampAsc(Long sessionId);
    List<ChatMessage> findByChatSessionIdOrderByIdAsc(Long sessionId);


}

