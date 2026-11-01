package com.example.securewebchatroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDto {

    private AuthDto() {}

    public record RegisterRequest(
            @NotBlank(message = "Username is required")
            @Size(min = 3, max = 30, message = "Username must be 3 to 30 characters")
            @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Username can only use letters, numbers, and underscores")
            String username,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
            @NotBlank(message = "Password is required")
            @Size(min = 8, max = 72, message = "Password must be 8 to 72 characters")
            String password) {}

    // `identifier` is the email or the username.
    public record LoginRequest(@NotBlank(message = "Email or username is required") String identifier,
                               @NotBlank(message = "Password is required") String password) {}

    public record AuthResponse(String token, UserDto.UserResponse user) {}
}
