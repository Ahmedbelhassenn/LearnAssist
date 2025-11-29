package com.example.LearnAssist.Dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleDto {
    private String title;
    private String content;
    private LocalDateTime publishedAt;
    public ArticleDto(String title, String content, LocalDateTime publishedAt) {
        this.title = title;
        this.content = content;
        this.publishedAt = publishedAt;
    }

}
