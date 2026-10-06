package com.example.LearnAssist.Services;

import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Models.ChatSession;

import java.util.HashMap;
import java.util.List;

/**
 * All methods are scoped to the participant identified by {@code participantEmail}
 * (the authenticated user). A session belonging to someone else is reported as not found
 * ({@link com.example.LearnAssist.Exceptions.ResourceNotFoundException}).
 */
public interface ChatSessionServices {

    HashMap<String,Object> createSession(String participantEmail, String question);
    List<ChatSession> getSessionsByParticipantEmail(String participantEmail);
    List<ChatMessage> getMessagesBySessionId(Long sessionId, String participantEmail);
    void deleteSession(Long sessionId, String participantEmail);
    void editSessionTitle(Long sessionId, String newTitle, String participantEmail);
}
