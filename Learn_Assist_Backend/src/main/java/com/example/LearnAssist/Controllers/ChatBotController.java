package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Dto.ChatRequest;
import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Repositories.ChatMessageRepository;
import com.example.LearnAssist.Services.ChatBotServices;
import com.example.LearnAssist.Services.ParticipantServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
public class ChatBotController {

    @Autowired
    ChatBotServices chatBotServices;

    @Autowired
    ParticipantServices participantServices;

    @Autowired
    ChatMessageRepository chatMessageRepository;

    @PreAuthorize("hasRole('PARTICIPANT')")
    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request, Principal principal) {
        HashMap<String, Object> map = new HashMap<>();
        try {
            String username = principal.getName();
            Long sessionId = request.getSessionId();

            String response = chatBotServices.askQuestion(request.getQuestion(), username, sessionId);
            map.put("response", response);

            return ResponseEntity.ok(map);
        }catch (Exception e){
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }


    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("/history")
    public ResponseEntity<List<ChatMessage>> getHistory(Principal principal) {
        Long userId = participantServices.getParticipantIdFromPrincipal(principal); // à implémenter si besoin
        List<ChatMessage> history = chatMessageRepository.findByParticipantIdOrderByTimestampAsc(userId);
        return ResponseEntity.ok(history);
    }

}