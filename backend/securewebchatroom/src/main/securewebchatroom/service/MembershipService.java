package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.RoomDto.MemberResponse;
import com.example.securewebchatroom.entity.ChatRoom;
import com.example.securewebchatroom.entity.RoomMembership;
import com.example.securewebchatroom.entity.User;
import com.example.securewebchatroom.enums.Role;
import com.example.securewebchatroom.exception.ApiException;
import com.example.securewebchatroom.repository.ChatRoomRepository;
import com.example.securewebchatroom.repository.RoomMembershipRepository;
import com.example.securewebchatroom.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Who is in which room, and the access checks every other service relies on.
@Service
public class MembershipService {

    private final RoomMembershipRepository memberships;
    private final ChatRoomRepository rooms;
    private final UserRepository users;

    public MembershipService(RoomMembershipRepository memberships, ChatRoomRepository rooms, UserRepository users) {
        this.memberships = memberships;
        this.rooms = rooms;
        this.users = users;
    }

    // ----- access checks -----

    public boolean isMember(Long roomId, Long userId) {
        return memberships.existsByRoomIdAndUserId(roomId, userId);
    }

    public boolean isOwner(Long roomId, Long userId) {
        return memberships.findByRoomIdAndUserId(roomId, userId).map(RoomMembership::isOwner).orElse(false);
    }

    public RoomMembership requireMember(Long roomId, Long userId) {
        return memberships.findByRoomIdAndUserId(roomId, userId)
                .orElseThrow(() -> ApiException.forbidden("You are not a member of this room"));
    }

    public RoomMembership requireOwner(Long roomId, Long userId) {
        RoomMembership membership = requireMember(roomId, userId);
        if (!membership.isOwner()) throw ApiException.forbidden("Only the room owner can do this");
        return membership;
    }

    // ----- join / leave -----

    @Transactional
    public MemberResponse join(Long roomId, Long userId) {
        ChatRoom room = findRoom(roomId);
        if (memberships.existsByRoomIdAndUserId(roomId, userId)) throw ApiException.conflict("You are already in this room");
        if (room.isPrivate()) throw ApiException.forbidden("This room is private. Ask the owner to add you.");
        User user = users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
        return MemberResponse.from(memberships.save(new RoomMembership(room, user, Role.MEMBER)));
    }

    @Transactional
    public void leave(Long roomId, Long userId) {
        RoomMembership membership = requireMember(roomId, userId);
        if (membership.isOwner()) throw ApiException.badRequest("Owners cannot leave their own room. Delete it instead.");
        memberships.delete(membership);
    }

    // ----- owner tools -----

    @Transactional(readOnly = true)
    public List<MemberResponse> list(Long roomId, Long userId) {
        requireMember(roomId, userId);
        return memberships.findByRoomId(roomId).stream().map(MemberResponse::from).toList();
    }

    @Transactional
    public MemberResponse add(Long roomId, Long ownerId, String username) {
        requireOwner(roomId, ownerId);
        User user = users.findByUsername(username.trim()).orElseThrow(() -> ApiException.notFound("No user with that username"));
        if (memberships.existsByRoomIdAndUserId(roomId, user.getId())) throw ApiException.conflict("That user is already in the room");
        return MemberResponse.from(memberships.save(new RoomMembership(findRoom(roomId), user, Role.MEMBER)));
    }

    @Transactional
    public MemberResponse changeRole(Long roomId, Long ownerId, Long targetUserId, Role role) {
        requireOwner(roomId, ownerId);
        if (targetUserId.equals(ownerId)) throw ApiException.badRequest("You cannot change your own role");
        RoomMembership target = memberships.findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> ApiException.notFound("That user is not in this room"));
        target.setRole(role);
        return MemberResponse.from(memberships.save(target));
    }

    @Transactional
    public void remove(Long roomId, Long ownerId, Long targetUserId) {
        requireOwner(roomId, ownerId);
        if (targetUserId.equals(ownerId)) throw ApiException.badRequest("You cannot remove yourself. Delete the room instead.");
        RoomMembership target = memberships.findByRoomIdAndUserId(roomId, targetUserId)
                .orElseThrow(() -> ApiException.notFound("That user is not in this room"));
        memberships.delete(target);
    }

    private ChatRoom findRoom(Long roomId) {
        return rooms.findById(roomId).orElseThrow(() -> ApiException.notFound("Room not found"));
    }
}
