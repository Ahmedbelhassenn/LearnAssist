package com.example.LearnAssist.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Participant extends User {
    private String educationLevel;


    @OneToMany(mappedBy = "participant")
    private List<InscriptionFormation> inscriptions;

}

