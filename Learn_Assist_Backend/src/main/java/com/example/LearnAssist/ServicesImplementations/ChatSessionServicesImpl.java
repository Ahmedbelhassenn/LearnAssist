package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Models.ChatSession;
import com.example.LearnAssist.Models.GeminiResponse;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.ChatMessageRepository;
import com.example.LearnAssist.Repositories.ChatSessionRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import com.example.LearnAssist.Services.ChatSessionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;


@Service
public class ChatSessionServicesImpl  implements ChatSessionServices {

    @Autowired
    ChatSessionRepository chatSessionRepository;
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    ChatMessageRepository chatMessageRepository;

    @Autowired
    RestClient restClient;

    @Value("${spring.ai.openai.api-key}")
    private String geminiApiKey;

    public HashMap<String, Object> createSession(String userEmail, String question) {
        Participant user = participantRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Appel à Gemini pour obtenir une réponse
        List<Map<String, Serializable>> initialPrompt = List.of(
                Map.of("role", "user", "parts", new Object[]{ Map.of("text", question) })
        );
        Map<String, Object> requestBody = Map.of("contents", initialPrompt);

        String answer;
        try {
            GeminiResponse response = restClient.post()
                    .uri("?key=" + geminiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiResponse.class);

            answer = (response != null && !response.getCandidates().isEmpty())
                    ? response.getCandidates().get(0).getContent().getParts().get(0).getText()
                    : "Aucune réponse trouvée.";

        } catch (Exception e) {
            e.printStackTrace();
            answer = "Erreur lors de l'appel à Gemini API.";
        }

        // 2. Générer un titre unique
        String baseTitle = generateTitleWithAI(question);
        String title = baseTitle;
        int suffix = 1;
        while (chatSessionRepository.existsByTitle(title)) {
            title = baseTitle + " (" + suffix + ")";
            suffix++;
        }

        // 3. Créer la session
        ChatSession session = new ChatSession();
        session.setTitle(title);
        session.setPreview(question.length() > 80 ? question.substring(0, 80) + "..." : question);
        session.setParticipant(user);
        session.setCreatedAt(LocalDateTime.now());
        chatSessionRepository.save(session);

        // 4. Enregistrer le message utilisateur
        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setTimestamp(LocalDateTime.now());
        userMessage.setParticipant(user);
        userMessage.setChatSession(session);
        chatMessageRepository.save(userMessage);

        // 5. Enregistrer la réponse de l'assistant
        ChatMessage botMessage = new ChatMessage();
        botMessage.setRole("assistant");
        botMessage.setContent(answer);
        botMessage.setTimestamp(LocalDateTime.now());
        botMessage.setParticipant(user);
        botMessage.setChatSession(session);
        chatMessageRepository.save(botMessage);

        // 6. Retour
        HashMap<String, Object> response = new HashMap<>();
        response.put("response", answer);
        response.put("sessionId", session.getId());

        return response;
    }


    public String generateTitleWithAI(String question) {
        String prompt = "Donne un titre court et clair (max 4 mots) pour cette question : " + question;

        List<Map<String, Serializable>> content = List.of(
                Map.of("role", "user", "parts", new Object[]{ Map.of("text", prompt) })
        );

        try {
            GeminiResponse response = restClient.post()
                    .uri("?key=" + geminiApiKey)
                    .body(Map.of("contents", content))
                    .retrieve()
                    .body(GeminiResponse.class);

            return (response != null && !response.getCandidates().isEmpty())
                    ? response.getCandidates().get(0).getContent().getParts().get(0).getText()
                    : "Nouvelle session";

        } catch (Exception e) {
            e.printStackTrace();
            return "Nouvelle session";
        }
    }

    @Override
    public Optional<ChatSession> getSession(Long sessionId) {
        return chatSessionRepository.findById(sessionId);
    }

    @Override
    public List<ChatSession> getSessions() {
        return chatSessionRepository.findAll();
    }

    @Override
    public void deleteSession(Long sessionId, Long participantId) {
        if (!chatSessionRepository.existsById(sessionId)) {
            throw new RuntimeException("Session not found");
        }
        if(Objects.equals(chatSessionRepository.findById(sessionId).get().getParticipant().getId(), participantId)){
            chatSessionRepository.deleteById(sessionId);
        }
        else{
            throw new RuntimeException("Participant not found");
        }
    }

    @Override
    public void editSessionTitle(Long sessionId, String newTitle, Long userId) {

        ChatSession session = chatSessionRepository.findById(sessionId).orElseThrow(
                () -> new RuntimeException("Session not found")
        );
        if (Objects.equals(session.getParticipant().getId(), userId)) {
            session.setTitle(newTitle);
            chatSessionRepository.save(session);
        }
        else {
            throw new RuntimeException("Participant not found");
        }

    }

    @Override
    public List<ChatSession> getSessionsByUserId(Long id){
        List<ChatSession> chatSessions = new ArrayList<>();
        for(ChatSession chatSession : chatSessionRepository.findAllByOrderByCreatedAtDesc()){
            if(chatSession.getParticipant().getId().equals(id)){
                chatSessions.add(chatSession);
            }
        }
        return chatSessions;

    }

    @Override
    public List<ChatMessage> getMessagesBySessionId(Long sessionId) {
        ChatSession chatSession = chatSessionRepository.findById(sessionId)
                .orElseThrow( () -> new RuntimeException("Session not found"));

        return chatMessageRepository.findByChatSessionIdOrderByIdAsc(sessionId);

    }

}