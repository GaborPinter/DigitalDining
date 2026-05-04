package com.DigitalDining.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

	@NotBlank(message = "Felhasználónév megadása kötelező")
    @Size(min = 3, max = 50, message = "A felhasználónévnek 3–50 karakter hosszúnak kell lennie")
    private String username;

    @NotBlank(message = "Jelszó megadása kötelező")
    @Size(min = 6, message = "A jelszónak legalább 6 karakter hosszúnak kell lennie")
    private String password;

    public LoginRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
