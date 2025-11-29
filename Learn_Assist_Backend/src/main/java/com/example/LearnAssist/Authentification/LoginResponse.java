package com.example.LearnAssist.Authentification;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private String jwtToken;
    private String email;
    private String photoName;
    private Long id;
    private String gender;
    private List<String> roles;

    public LoginResponse(String email, List<String> roles, String jwtToken, Long id, String photoName, String gender) {
        this.id=id;
        this.email = email;
        this.roles = roles;
        this.jwtToken = jwtToken;
        this.photoName = photoName;
        this.gender = gender;
    }
}
