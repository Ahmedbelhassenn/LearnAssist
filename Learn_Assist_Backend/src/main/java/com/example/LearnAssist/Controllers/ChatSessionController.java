package com.example.LearnAssist.Controllers;

import com.example.LearnAssist.Exceptions.ResourceNotFoundException;
import com.example.LearnAssist.Models.ChatMessage;
import com.example.LearnAssist.Models.ChatSession;
import com.example.LearnAssist.Services.ChatSessionServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;

/**
 * Chat sessions of the authenticated participant. Every endpoint is scoped to the caller:
 * a session belonging to another participant answers 404, exactly like a missing one.
 * The former "GET /api/sessions" (all sessions of all participants) was removed.
 */
@RestController
@RequestMapping("api/sessions")
public class ChatSessionController {
    @Autowired
    ChatSessionServices chatSessionServices;

    @PreAuthorize("hasRole('PARTICIPANT')")
    @GetMapping("/{id}")
    public ResponseEntity<?> getChatSession(Principal principal, @PathVariable Long id) {
        HashMap<String,Object> map = new HashMap<>();
        try {
            List<ChatMessage> chatSession=chatSessionServices.getMessagesBySessionId(id, principal.getName());
            map.put("chatSession",chatSession);
            return ResponseEntity.ok(map);
        } catch (ResourceNotFoundException e) {
            return notFound(e);
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
            List<ChatSession> sessions=chatSessionServices.getSessionsByParticipantEmail(principal.getName());
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
            chatSessionServices.deleteSession(id, principal.getName());
            map.put("message","session deleted successfully");
            return ResponseEntity.ok(map);
        } catch (ResourceNotFoundException e) {
            return notFound(e);
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
            chatSessionServices.editSessionTitle(id, question, principal.getName());
            map.put("message","session updated successfully");
            return ResponseEntity.ok(map);
        } catch (ResourceNotFoundException e) {
            return notFound(e);
        }
        catch (Exception e) {
            map.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(map);
        }
    }

    private static ResponseEntity<?> notFound(ResourceNotFoundException e) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(map);
    }

}
