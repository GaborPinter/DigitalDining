package com.DigitalDining.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordRequest {

	@NotBlank(message = "Új jelszó megadása kötelező")
    @Size(min = 6, message = "A jelszónak legalább 6 karakter hosszúnak kell lennie")
    private String newPassword;

    @NotBlank(message = "Az új jelszó megerősítése kötelező")
    @Size(min = 6, message = "A jelszónak legalább 6 karakter hosszúnak kell lennie")
    private String confirmNewPassword;

    public ResetPasswordRequest() {
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfirmNewPassword() {
        return confirmNewPassword;
    }

    public void setConfirmNewPassword(String confirmNewPassword) {
        this.confirmNewPassword = confirmNewPassword;
    }
}
