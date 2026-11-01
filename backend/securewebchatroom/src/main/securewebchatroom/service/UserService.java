package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.UserDto.StatsResponse;
import com.example.securewebchatroom.dto.UserDto.UpdateProfileRequest;
import com.example.securewebchatroom.dto.UserDto.UserResponse;
import com.example.securewebchatroom.entity.User;
import com.example.securewebchatroom.enums.ReviewStatus;
import com.example.securewebchatroom.enums.Role;
import com.example.securewebchatroom.exception.ApiException;
import com.example.securewebchatroom.repository.FlaggedMessageRepository;
import com.example.securewebchatroom.repository.RoomMembershipRepository;
import com.example.securewebchatroom.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final List<String> THEMES = List.of("clay", "sage", "caramel");
    private static final List<String> VIEWS = List.of("grid", "list");

    private final UserRepository users;
    private final RoomMembershipRepository memberships;
    private final FlaggedMessageRepository flagged;

    public UserService(UserRepository users, RoomMembershipRepository memberships, FlaggedMessageRepository flagged) {
        this.users = users;
        this.memberships = memberships;
        this.flagged = flagged;
    }

    public UserResponse getProfile(Long userId) {
        return UserResponse.from(find(userId));
    }

    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = find(userId);
        if (request.displayName() != null && !request.displayName().isBlank()) user.setDisplayName(request.displayName().trim());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.dashboardTheme() != null) {
            if (!THEMES.contains(request.dashboardTheme())) throw ApiException.badRequest("Unknown accent color");
            user.setDashboardTheme(request.dashboardTheme());
        }
        if (request.defaultView() != null) {
            if (!VIEWS.contains(request.defaultView())) throw ApiException.badRequest("Unknown room view");
            user.setDefaultView(request.defaultView());
        }
        if (request.showFlagCount() != null) user.setShowFlagCount(request.showFlagCount());
        return UserResponse.from(users.save(user));
    }

    public StatsResponse stats(Long userId) {
        return new StatsResponse(
                memberships.countByUserIdAndRole(userId, Role.OWNER),
                memberships.countByUserIdAndRole(userId, Role.MEMBER),
                flagged.countByMessageRoomOwnerIdAndReviewStatus(userId, ReviewStatus.PENDING));
    }

    // The database removes the user's rooms, memberships and messages (ON DELETE CASCADE).
    public void deleteAccount(Long userId) {
        users.deleteById(userId);
    }

    private User find(Long userId) {
        return users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found"));
    }
}
