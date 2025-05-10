package org.example.kursovoi.classes;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String phoneEmail;
    private String password;

    public LoginRequest(String phoneEmail, String password) {
        this.phoneEmail = phoneEmail;
        this.password = password;
    }
}