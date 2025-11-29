package com.example.LearnAssist.Dto;


import lombok.Data;

@Data
public class InscriptionFormationRequest {
    private String participantName;
    private String participantEmail;
    private String participantPhone;
    private Long formationId;
}
