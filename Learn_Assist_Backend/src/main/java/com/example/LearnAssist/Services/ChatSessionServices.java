package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Models.ChatSession;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface ChatSessionServices {

    HashMap<String,Object> createSession(String userEmail, String question);
    Optional<ChatSession> getSession(Long sessionId);
    List<ChatSession> getSessions();
    List<ChatSession> getSessionsByUserId(Long id);
    List<ChatMessage> getMessagesBySessionId(Long sessionId);
    void deleteSession(Long sessionId, Long userId);
    void editSessionTitle(Long sessionId, String newTitle, Long userId);
}
