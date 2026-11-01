package com.example.securewebchatroom.controller;

import com.example.securewebchatroom.dto.ModerationDto.FlaggedResponse;
import com.example.securewebchatroom.dto.ModerationDto.ReviewRequest;
import com.example.securewebchatroom.dto.ModerationDto.SettingRequest;
import com.example.securewebchatroom.dto.ModerationDto.SettingResponse;
import com.example.securewebchatroom.service.ModerationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Everything here is owner-only (the service checks it).
@RestController
@RequestMapping("/api")
public class ModerationController {

    private final ModerationService moderationService;

    public ModerationController(ModerationService moderationService) {
        this.moderationService = moderationService;
    }

    @GetMapping("/rooms/{roomId}/moderation")
    public SettingResponse getSettings(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        return moderationService.getSettings(roomId, userId);
    }

    @PutMapping("/rooms/{roomId}/moderation")
    public SettingResponse updateSettings(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId,
                                          @Valid @RequestBody SettingRequest request) {
        return moderationService.updateSettings(roomId, userId, request);
    }

    // Flagged messages and the moderation log are the same list: filter by reviewStatus on the frontend.
    @GetMapping("/rooms/{roomId}/moderation/flags")
    public List<FlaggedResponse> flagged(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        return moderationService.listFlagged(roomId, userId);
    }

    // Approve, restore, or delete a flagged message
    @PutMapping("/moderation/flags/{flagId}")
    public FlaggedResponse review(@AuthenticationPrincipal Long userId, @PathVariable("flagId") Long flagId,
                                  @Valid @RequestBody ReviewRequest request) {
        return moderationService.review(flagId, userId, request.decision());
    }
}
