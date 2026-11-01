package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.AuthDto.AuthResponse;
import com.example.securewebchatroom.dto.AuthDto.LoginRequest;
import com.example.securewebchatroom.dto.AuthDto.RegisterRequest;
import com.example.securewebchatroom.dto.UserDto.UserResponse;
import com.example.securewebchatroom.entity.User;
import com.example.securewebchatroom.exception.ApiException;
import com.example.securewebchatroom.repository.UserRepository;
import com.example.securewebchatroom.security.JwtService;
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
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();
        if (users.existsByUsername(username)) throw ApiException.conflict("That username is taken");
        if (users.existsByEmail(email)) throw ApiException.conflict("That email is already registered");

        User user = users.save(new User(username, email, passwordEncoder.encode(request.password())));
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = request.identifier().trim();
        User user = users.findByUsername(identifier)
                .or(() -> users.findByEmail(identifier.toLowerCase()))
                .orElseThrow(() -> ApiException.unauthorized("Wrong email/username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("Wrong email/username or password");
        }
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }
}
