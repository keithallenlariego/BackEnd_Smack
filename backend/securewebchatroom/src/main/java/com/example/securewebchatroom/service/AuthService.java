package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.AuthDto.AuthResponse;
import com.example.securewebchatroom.dto.AuthDto.LoginRequest;
import com.example.securewebchatroom.dto.AuthDto.RegisterRequest;
import com.example.securewebchatroom.dto.UserDto.UserResponse;
import com.example.securewebchatroom.entity.User;
import com.example.securewebchatroom.exception.ApiException;
import com.example.securewebchatroom.repository.UserRepository;
import com.example.securewebchatroom.security.JwtService;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        String username = request.username().trim();
        if (users.existsByUsernameIgnoreCase(username)) throw ApiException.conflict("That username is taken");
        if (users.existsByEmailIgnoreCase(email)) throw ApiException.conflict("That email is already registered");

        User user = users.save(new User(username, email, passwordEncoder.encode(request.password())));
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        String lowercaseIdentifier = identifier.toLowerCase(Locale.ROOT);
        User user = users.findByUsernameIgnoreCase(identifier)
                .or(() -> users.findByEmailIgnoreCase(lowercaseIdentifier))
                .orElseThrow(() -> ApiException.unauthorized("Wrong email/username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Wrong email/username or password");
        }
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }
}
