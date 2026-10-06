package com.example.LearnAssist.Dto;

import com.example.LearnAssist.Models.Participant;

import java.util.Date;

/** Participant as returned by the API. Never contains the password hash. */
public record ParticipantResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender,
        Date dateOfBirth,
        String city,
        String profilePhoto,
        String educationLevel,
        String status,
        String role
) {
    public static ParticipantResponse from(Participant participant) {
        return new ParticipantResponse(
                participant.getId(),
                participant.getFirstName(),
                participant.getLastName(),
                participant.getEmail(),
                participant.getPhone(),
                participant.getGender(),
                participant.getDateOfBirth(),
                participant.getCity(),
                participant.getProfilePhoto(),
                participant.getEducationLevel(),
                participant.getStatus(),
                participant.getRole()
        );
    }
}
