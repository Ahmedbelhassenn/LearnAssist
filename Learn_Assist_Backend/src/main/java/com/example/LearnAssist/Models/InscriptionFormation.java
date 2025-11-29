package com.example.LearnAssist.Models;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table( name = "inscription_formation", uniqueConstraints = @UniqueConstraint(columnNames = {"formation_id", "participant_id"}))
public class InscriptionFormation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "formation_id" , nullable = false)
    private Formation formation;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "participant_id" , nullable = false)
    private Participant participant;

    private String status; // PENDING, APPROVED, REJECTED
    private LocalDateTime createdAt;
    private Long rate;
    private String updatedEmail;
    private String updatedName;
    private String updatedPhone;

}
