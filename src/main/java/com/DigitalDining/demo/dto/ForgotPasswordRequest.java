package com.DigitalDining.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ForgotPasswordRequest {

	@NotBlank(message = "Felhasználónév megadása kötelező")
    @Size(min = 3, max = 50, message = "A felhasználónévnek 3–50 karakter hosszúnak kell lennie")
    private String username;

    @NotBlank(message = "Email megadása kötelező")
    @Email(message = "Érvényes emailt adjon meg")
    private String email;

    public ForgotPasswordRequest() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
