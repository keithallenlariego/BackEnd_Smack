package com.example.securewebchatroom.controller;

import com.example.securewebchatroom.dto.MessageDto.MessageResponse;
import com.example.securewebchatroom.dto.MessageDto.SendMessageRequest;
import com.example.securewebchatroom.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms/{roomId}/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping
    public List<MessageResponse> history(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        return messageService.history(roomId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId,
                                @Valid @RequestBody SendMessageRequest request) {
        return messageService.send(roomId, userId, request.content());
    }
}
