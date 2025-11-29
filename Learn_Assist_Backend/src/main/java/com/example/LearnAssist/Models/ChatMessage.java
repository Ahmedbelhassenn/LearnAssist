package com.example.LearnAssist.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "user" ou "assistant"
    private String role;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    // Relation avec User (participant, instructeur…)
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "participant_id")
    private Participant participant;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "chat_session_id")
    private ChatSession chatSession;
}
