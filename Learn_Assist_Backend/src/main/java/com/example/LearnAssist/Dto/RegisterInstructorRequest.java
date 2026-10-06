package com.example.LearnAssist.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;

/**
 * Instructor sign-up payload. It intentionally has NO id, role, status or profilePhoto:
 * those are decided by the server. Unknown JSON properties are ignored, and a brand-new
 * entity is always created from these fields.
 */
@Data
public class RegisterInstructorRequest {
    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Size(max = 30)
    private String phone;

    @NotBlank
    @Email
    @Size(max = 255)
    private String email;

    /** BCrypt only uses the first 72 bytes, hence the upper bound. */
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @Size(max = 20)
    private String gender;

    @Past
    private Date dateOfBirth;

    @NotBlank
    @Size(max = 100)
    private String city;

    @Size(max = 1000)
    private String bio;

    @Size(max = 255)
    private String speciality;
}
