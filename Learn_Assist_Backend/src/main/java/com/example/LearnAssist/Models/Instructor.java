package com.example.LearnAssist.Models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
public class Instructor extends User {
    @Column(length = 1000)
    private String bio;
    private String speciality;
}
