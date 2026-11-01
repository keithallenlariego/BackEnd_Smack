package com.example.securewebchatroom.dto;

import com.example.securewebchatroom.entity.User;
import jakarta.validation.constraints.Size;

public class UserDto {

    private UserDto() {}

    public record UserResponse(Long id, String username, String email, String displayName, String bio,
                               String dashboardTheme, String defaultView, boolean showFlagCount) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getDisplayName(), u.getBio(),
                    u.getDashboardTheme(), u.getDefaultView(), u.isShowFlagCount());
        }
    }

    // Username and email stay read-only for now. Send only the fields you want to change.
    public record UpdateProfileRequest(@Size(max = 50, message = "Display name is too long") String displayName,
                                       @Size(max = 255, message = "Bio is too long") String bio,
                                       String dashboardTheme, String defaultView, Boolean showFlagCount) {}

    // The three numbers on the dashboard cards.
    public record StatsResponse(long roomsOwned, long roomsJoined, long flaggedToReview) {}
}
