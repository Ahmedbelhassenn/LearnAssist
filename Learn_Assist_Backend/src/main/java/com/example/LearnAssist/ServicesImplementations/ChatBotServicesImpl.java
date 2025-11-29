package com.example.LearnAssist.ServicesImplementations;

import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Models.ChatSession;
import com.example.LearnAssist.Models.GeminiResponse;
import com.example.LearnAssist.Models.Participant;
import com.example.LearnAssist.Repositories.ChatMessageRepository;
import com.example.LearnAssist.Repositories.ChatSessionRepository;
import com.example.LearnAssist.Repositories.ParticipantRepository;
import com.example.LearnAssist.Services.ChatBotServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ChatBotServicesImpl  implements ChatBotServices {

    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    RestClient restClient;
    @Autowired
    ParticipantRepository participantRepository;
    @Autowired
    ChatSessionRepository chatSessionRepository;

    @Value("${spring.ai.openai.api-key}")
    private String geminiApiKey;


    public String askQuestion(String question, String userEmail, Long sessionId) {
        // Récupérer l'utilisateur
        Participant user = participantRepository.findByEmail(userEmail).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        ChatSession session = chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session non trouvée"));

        if (!session.getParticipant().getEmail().equals(userEmail)) {
            throw new AccessDeniedException("Session non autorisée");
        }

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setTimestamp(LocalDateTime.now());
        userMessage.setParticipant(user);
        userMessage.setChatSession(session);
        chatMessageRepository.save(userMessage);

        // Récupérer l'historique
        List<ChatMessage> history = chatMessageRepository.findByChatSessionIdOrderByTimestampAsc(sessionId);

        // Convertir pour l'API Gemini
        List<Map<String, Serializable>> contents = history.stream()
                .map(msg -> Map.of(
                        "role", msg.getRole(),
                        "parts", new Object[]{ Map.of("text", msg.getContent()) }
                ))
                .toList();

        Map<String, Object> requestBody = Map.of("contents", contents);

        try {
            var response = restClient.post()
                    .uri("?key=" + geminiApiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(GeminiResponse.class);

            String answer = (response != null && !response.getCandidates().isEmpty())
                    ? response.getCandidates().get(0).getContent().getParts().get(0).getText()
                    : "Aucune réponse trouvée.";

            // Enregistrer la réponse du bot
            ChatMessage botMessage = new ChatMessage();
            botMessage.setRole("assistant");
            botMessage.setContent(answer);
            botMessage.setTimestamp(LocalDateTime.now());
            botMessage.setParticipant(user);
            botMessage.setChatSession(session);
            chatMessageRepository.save(botMessage);

            return answer;

        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de l'appel à Gemini API.";
        }
    }

}

