package com.example.LearnAssist.Dto;


import lombok.Data;

@Data
public class ChatResponse {
    private String answer;
    public ChatResponse(String answer) {
        this.answer = answer;
    }
}
