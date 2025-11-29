package com.example.LearnAssist.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String title;
    @Column(length = 2000)
    private String description;
    private String videoUrl;
    private String formationCategory;
    private String formationLanguage;
    private String price;
    private String formationDuration;
    private String formationCreationDate;
    private String formationStatus;
    private float rate;
    private String formationLevel;
    private String imageFileName;
    private String videoFileName;
    private String certification;
    private String emailInstructor;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL)
    private List<Course> courses;

    @OneToMany(mappedBy = "formation")
    private List<InscriptionFormation> inscriptions;

}
