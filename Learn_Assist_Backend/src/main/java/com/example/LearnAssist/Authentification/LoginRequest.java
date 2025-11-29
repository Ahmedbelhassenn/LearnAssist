package com.example.LearnAssist.Authentification;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
