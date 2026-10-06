package com.example.LearnAssist.Models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@MappedSuperclass
@Data
public abstract class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String phone;

    @Column(unique = true, nullable = false)
    private String email;

    private String gender;
    private Date dateOfBirth;
    private String profilePhoto;

    @Column(nullable = false)
    private String city;

    private String status;

    /**
     * Safety net: the hash can be read from a request body but is never written to JSON,
     * even if an entity is accidentally returned by a controller.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role; // ADMIN, INSTRUCTOR, PARTICIPANT
}

