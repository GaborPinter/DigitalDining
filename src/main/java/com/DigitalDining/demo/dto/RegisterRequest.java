package com.DigitalDining.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

	@NotBlank(message = "Felhasználónév megadása kötelező")
	@Size(min = 3, max = 50)
	private String username;

	@NotBlank(message = "Email megadása kötelező")
	@Email(message = "Érvényes emailt adjon meg")
	private String email;

	@NotBlank(message = "Jelszó megadása kötelező")
	@Size(min = 6, message = "A jelszónak legalább 6 karakter hosszúnak kell lennie")
	private String password;

	public RegisterRequest() {
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
