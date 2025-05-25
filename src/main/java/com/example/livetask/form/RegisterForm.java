package com.example.livetask.form;

import jakarta.validation.constraints.NotBlank;

public class RegisterForm {

    @NotBlank(message = "{user.username.required}")
    private String username;

    @NotBlank(message = "{user.email.required}")
    private String email;

    @NotBlank(message = "{user.password.required}")
    private String password;

    @NotBlank(message = "{user.confirmPassword.required}")
    private String confirmPassword;

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

