package com.example.securewebchatroom.security;

import com.example.securewebchatroom.service.MembershipService;
import java.util.List;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

// Guards the WebSocket the same way the REST API is guarded:
//  - CONNECT must carry a valid token
//  - SUBSCRIBE to /topic/rooms/{id} needs membership, and .../moderation needs the owner
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final MembershipService membershipService;

    public StompAuthInterceptor(JwtService jwtService, MembershipService membershipService) {
        this.jwtService = jwtService;
        this.membershipService = membershipService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) return message;

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) throw new MessagingException("Missing token");
            try {
                Long userId = jwtService.parseUserId(header.substring(7));
                accessor.setUser(new UsernamePasswordAuthenticationToken(userId, null, List.of()));
            } catch (RuntimeException ex) {
                throw new MessagingException("Invalid token");
            }
        } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (!(accessor.getUser() instanceof Authentication auth) || !(auth.getPrincipal() instanceof Long userId)) {
                throw new MessagingException("Not logged in");
            }
            checkSubscription(accessor.getDestination(), userId);
        }
        return message;
    }

    private void checkSubscription(String destination, Long userId) {
        // /topic/rooms/12  or  /topic/rooms/12/moderation
        if (destination == null || !destination.startsWith("/topic/rooms/")) throw new MessagingException("Unknown topic");
        String[] parts = destination.substring("/topic/rooms/".length()).split("/");
        Long roomId;
        try {
            roomId = Long.valueOf(parts[0]);
        } catch (NumberFormatException ex) {
            throw new MessagingException("Unknown topic");
        }
        boolean ownerOnly = parts.length > 1 && parts[1].equals("moderation");
        boolean allowed = ownerOnly ? membershipService.isOwner(roomId, userId) : membershipService.isMember(roomId, userId);
        if (!allowed) throw new MessagingException("Access denied");
    }
}
