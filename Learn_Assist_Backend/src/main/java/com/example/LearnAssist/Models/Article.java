package com.example.LearnAssist.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Lob
    private String content;

    private String imageFileName;

    private LocalDateTime publishedAt;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "instructor_id")
    private Instructor instructor;
}
