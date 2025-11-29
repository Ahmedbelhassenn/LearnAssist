package com.example.LearnAssist.Dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String question;
    private Long sessionId;
}
