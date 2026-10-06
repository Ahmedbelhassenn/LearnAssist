package com.example.LearnAssist.Dto;

import com.example.LearnAssist.Models.Instructor;

import java.util.Date;

/** Instructor as returned by the API. Never contains the password hash. */
public record InstructorResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String gender,
        Date dateOfBirth,
        String city,
        String profilePhoto,
        String bio,
        String speciality,
        String status,
        String role
) {
    public static InstructorResponse from(Instructor instructor) {
        return new InstructorResponse(
                instructor.getId(),
                instructor.getFirstName(),
                instructor.getLastName(),
                instructor.getEmail(),
                instructor.getPhone(),
                instructor.getGender(),
                instructor.getDateOfBirth(),
                instructor.getCity(),
                instructor.getProfilePhoto(),
                instructor.getBio(),
                instructor.getSpeciality(),
                instructor.getStatus(),
                instructor.getRole()
        );
    }
}
