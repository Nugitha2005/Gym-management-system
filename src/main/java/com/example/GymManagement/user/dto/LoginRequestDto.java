package com.example.GymManagement.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequestDto {

    // 1. Email acts as the username for login
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // 2. Raw password typed by the user (compared against the BCrypt hash)
    @NotBlank(message = "Password is required")
    private String password;
}
