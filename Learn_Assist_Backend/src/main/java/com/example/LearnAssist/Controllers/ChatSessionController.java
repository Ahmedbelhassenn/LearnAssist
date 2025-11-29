package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Models.ChatSession;
import com.example.LearnAssist.Services.ChatSessionServices;
import com.example.LearnAssist.Services.ParticipantServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api/sessions")
public class ChatSessionController {
    @Autowired
    ChatSessionServices chatSessionServices;
    @Autowired
    ParticipantServices participantServices;

    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatSession( @PathVariable Long id) {
        HashMap<String,Object> map = new HashMap<>();
        try {
            List<ChatMessage> chatSession=chatSessionServices.getMessagesBySessionId(id);
            map.put("chatSession",chatSession);
            return ResponseEntity.ok(map);

        }catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("")
    public ResponseEntity<?> getChatSessions() {
        HashMap<String,Object> map = new HashMap<>();
        try{
            List<ChatSession> sessions=chatSessionServices.getSessions();
            map.put("sessions", sessions);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

    @PreAuthorize("hasRole('PARTICIPANT')")
    @PostMapping("")
    public ResponseEntity<?> createSession(Principal principal, @RequestBody String question) {
        HashMap<String,Object> map = new HashMap<>();
        try {
             map = chatSessionServices.createSession(principal.getName(),question);
            return ResponseEntity.ok(map);
        } catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }

    }

    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("/list")
    public ResponseEntity<?> getSessions(Principal principal) {
        HashMap<String,Object> map = new HashMap<>();
        try {
            Long userId=participantServices.getParticipantIdFromPrincipal(principal);
            List<ChatSession> sessions=chatSessionServices.getSessionsByUserId(userId);
            map.put("sessions", sessions);
            return ResponseEntity.ok(map);
        }catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

    @PreAuthorize("hasRole('PARTICIPANT')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSession(Principal principal , @PathVariable Long id) {
        HashMap<String,Object> map = new HashMap<>();
        try {
            Long userId=participantServices.getParticipantIdFromPrincipal(principal);
            chatSessionServices.deleteSession(id, userId);
            map.put("message","session deleted successfully");
            return ResponseEntity.ok(map);
        }catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

    @PreAuthorize("hasRole('PARTICIPANT')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSession(Principal principal, @PathVariable Long id, @RequestBody String question) {
        HashMap<String,Object> map = new HashMap<>();
        try {
            Long userId=participantServices.getParticipantIdFromPrincipal(principal);
            chatSessionServices.editSessionTitle(id, question, userId);
            map.put("message","session updated successfully");
            return ResponseEntity.ok(map);
        }
        catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

}
