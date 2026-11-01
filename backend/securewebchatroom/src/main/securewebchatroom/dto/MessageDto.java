package com.example.securewebchatroom.dto;

import com.example.securewebchatroom.entity.Message;
import com.example.securewebchatroom.enums.MessageStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class MessageDto {

    private MessageDto() {}

    public record SendMessageRequest(@NotBlank(message = "Message cannot be empty")
                                     @Size(max = 2000, message = "Message is too long") String content) {}

    // `note` is only filled for the sender when the AI moderator warned or hid their message.
    public record MessageResponse(Long id, Long roomId, Long senderId, String senderName, String content,
                                  MessageStatus status, Instant sentAt, String note) {
        public static MessageResponse from(Message m, String note) {
            return new MessageResponse(m.getId(), m.getRoom().getId(), m.getSender().getId(),
                    m.getSender().getDisplayName(), m.getContent(), m.getStatus(), m.getSentAt(), note);
        }
    }
}
