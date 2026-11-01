package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.RoomDto.RoomRequest;
import com.example.securewebchatroom.dto.RoomDto.RoomResponse;
import com.example.securewebchatroom.entity.ChatRoom;
import com.example.securewebchatroom.entity.ModerationSetting;
import com.example.securewebchatroom.entity.RoomMembership;
import com.example.securewebchatroom.entity.User;
import com.example.securewebchatroom.enums.MessageStatus;
import com.example.securewebchatroom.enums.Role;
import com.example.securewebchatroom.enums.Visibility;
import com.example.securewebchatroom.exception.ApiException;
import com.example.securewebchatroom.repository.ChatRoomRepository;
import com.example.securewebchatroom.repository.MessageRepository;
import com.example.securewebchatroom.repository.ModerationSettingRepository;
import com.example.securewebchatroom.repository.RoomMembershipRepository;
import com.example.securewebchatroom.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomService {

    private final ChatRoomRepository rooms;
    private final RoomMembershipRepository memberships;
    private final ModerationSettingRepository settings;
    private final MessageRepository messages;
    private final UserRepository users;
    private final MembershipService access;

    public RoomService(ChatRoomRepository rooms, RoomMembershipRepository memberships, ModerationSettingRepository settings,
                       MessageRepository messages, UserRepository users, MembershipService access) {
        this.rooms = rooms;
        this.memberships = memberships;
        this.settings = settings;
        this.messages = messages;
        this.users = users;
        this.access = access;
    }

    // C - create: the creator becomes the OWNER, and the room gets its moderation settings.
    @Transactional
    public RoomResponse create(Long userId, RoomRequest request) {
        User owner = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        ChatRoom room = rooms.save(new ChatRoom(owner, request.name().trim(), request.description(), request.visibility()));
        memberships.save(new RoomMembership(room, owner, Role.OWNER));
        settings.save(new ModerationSetting(room, request.aiModeratorEnabled()));
        return toResponse(room, Role.OWNER);
    }

    // R - rooms the user belongs to (the dashboard and the sidebar)
    @Transactional(readOnly = true)
    public List<RoomResponse> myRooms(Long userId) {
        return memberships.findByUserId(userId).stream()
                .map(m -> toResponse(m.getRoom(), m.getRole()))
                .toList();
    }

    // R - public rooms the user has not joined yet
    @Transactional(readOnly = true)
    public List<RoomResponse> publicRooms(Long userId) {
        return rooms.findByVisibility(Visibility.PUBLIC).stream()
                .filter(room -> !memberships.existsByRoomIdAndUserId(room.getId(), userId))
                .map(room -> toResponse(room, null))
                .toList();
    }

    // R - one room. A private room is only visible to its members (403 = the "Access denied" screen).
    @Transactional(readOnly = true)
    public RoomResponse get(Long roomId, Long userId) {
        ChatRoom room = find(roomId);
        Role role = memberships.findByRoomIdAndUserId(roomId, userId).map(RoomMembership::getRole).orElse(null);
        if (room.isPrivate() && role == null) throw ApiException.forbidden("This room is private");
        return toResponse(room, role);
    }

    // U - owner only
    @Transactional
    public RoomResponse update(Long roomId, Long userId, RoomRequest request) {
        access.requireOwner(roomId, userId);
        ChatRoom room = find(roomId);
        room.setName(request.name().trim());
        room.setDescription(request.description());
        room.setVisibility(request.visibility());
        return toResponse(rooms.save(room), Role.OWNER);
    }

    // D - owner only. The database removes the members and messages (ON DELETE CASCADE).
    @Transactional
    public void delete(Long roomId, Long userId) {
        access.requireOwner(roomId, userId);
        rooms.deleteById(roomId);
    }

    private ChatRoom find(Long roomId) {
        return rooms.findById(roomId).orElseThrow(() -> ApiException.notFound("Room not found"));
    }

    private RoomResponse toResponse(ChatRoom room, Role role) {
        String lastMessage = messages.findFirstByRoomIdAndStatusOrderBySentAtDesc(room.getId(), MessageStatus.VISIBLE)
                .map(m -> m.getSender().getDisplayName() + ": " + m.getContent())
                .orElse(null);
        return new RoomResponse(room.getId(), room.getName(), room.getDescription(), room.getVisibility(),
                room.getOwner().getId(), memberships.countByRoomId(room.getId()), role, lastMessage);
    }
}
