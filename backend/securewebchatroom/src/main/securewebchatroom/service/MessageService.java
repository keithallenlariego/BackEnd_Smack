package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.MessageDto.MessageResponse;
import com.example.securewebchatroom.entity.Message;
import com.example.securewebchatroom.entity.RoomMembership;
import com.example.securewebchatroom.enums.MessageStatus;
import com.example.securewebchatroom.repository.MessageRepository;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageService {

    private final MessageRepository messages;
    private final MembershipService access;
    private final ModerationService moderation;
    private final SimpMessagingTemplate broker;

    public MessageService(MessageRepository messages, MembershipService access, ModerationService moderation, SimpMessagingTemplate broker) {
        this.messages = messages;
        this.access = access;
        this.moderation = moderation;
        this.broker = broker;
    }

    // Chat history. Members see visible messages; the owner also sees the ones the AI hid.
    @Transactional(readOnly = true)
    public List<MessageResponse> history(Long roomId, Long userId) {
        RoomMembership membership = access.requireMember(roomId, userId);
        List<MessageStatus> statuses = membership.isOwner()
                ? List.of(MessageStatus.VISIBLE, MessageStatus.HIDDEN)
                : List.of(MessageStatus.VISIBLE);
        return messages.findByRoomIdAndStatusInOrderBySentAtAsc(roomId, statuses).stream()
                .map(m -> MessageResponse.from(m, null))
                .toList();
    }

    // Saves the message, runs the AI moderator, then sends it live to everyone subscribed to the room.
    @Transactional
    public MessageResponse send(Long roomId, Long userId, String content) {
        RoomMembership membership = access.requireMember(roomId, userId);
        Message message = messages.save(new Message(membership.getRoom(), membership.getUser(), content.trim()));

        String note = moderation.moderate(message); // may hide the message

        if (message.getStatus() == MessageStatus.VISIBLE) {
            broker.convertAndSend("/topic/rooms/" + roomId, MessageResponse.from(message, null));
        }
        return MessageResponse.from(message, note);
    }
}
