package com.example.securewebchatroom.controller;

import com.example.securewebchatroom.dto.UserDto.StatsResponse;
import com.example.securewebchatroom.dto.UserDto.UpdateProfileRequest;
import com.example.securewebchatroom.dto.UserDto.UserResponse;
import com.example.securewebchatroom.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public UserResponse me(@AuthenticationPrincipal Long userId) {
        return userService.getProfile(userId);
    }

    @PutMapping
    public UserResponse update(@AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(userId, request);
    }

    @GetMapping("/stats")
    public StatsResponse stats(@AuthenticationPrincipal Long userId) {
        return userService.stats(userId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId) {
        userService.deleteAccount(userId);
    }
}
