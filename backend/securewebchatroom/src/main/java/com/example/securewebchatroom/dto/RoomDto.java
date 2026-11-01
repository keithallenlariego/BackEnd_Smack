package com.example.securewebchatroom.dto;

import com.example.securewebchatroom.entity.RoomMembership;
import com.example.securewebchatroom.enums.Role;
import com.example.securewebchatroom.enums.Visibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class RoomDto {

    private RoomDto() {}

    // `aiModeratorEnabled` is only used when creating a room. Updating it happens on the moderation endpoint.
    public record RoomRequest(@NotBlank(message = "Room name is required") @Size(max = 100, message = "Room name is too long") String name,
                              @Size(max = 255, message = "Description is too long") String description,
                              @NotNull(message = "Choose public or private") Visibility visibility,
                              boolean aiModeratorEnabled) {}

    // `role` is the caller's role in the room (null if they are not a member).
    public record RoomResponse(Long id, String name, String description, Visibility visibility, Long ownerId,
                               long memberCount, Role role, String lastMessage) {}

    public record MemberResponse(Long userId, String username, String displayName, Role role, Instant joinedAt) {
        public static MemberResponse from(RoomMembership m) {
            return new MemberResponse(m.getUser().getId(), m.getUser().getUsername(), m.getUser().getDisplayName(),
                    m.getRole(), m.getJoinedAt());
        }
    }

    public record AddMemberRequest(@NotBlank(message = "Username is required") String username) {}

    public record ChangeRoleRequest(@NotNull(message = "Choose a role") Role role) {}
}
