package com.example.securewebchatroom.dto;

import com.example.securewebchatroom.entity.FlaggedMessage;
import com.example.securewebchatroom.entity.ModerationSetting;
import com.example.securewebchatroom.enums.FlagAction;
import com.example.securewebchatroom.enums.FlagReason;
import com.example.securewebchatroom.enums.ModerationAction;
import com.example.securewebchatroom.enums.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public class ModerationDto {

    private ModerationDto() {}

    public record SettingRequest(@NotNull(message = "Turn the AI moderator on or off") Boolean aiEnabled,
                                 @NotNull(message = "Choose what happens to flagged messages") ModerationAction actionMode) {}

    public record SettingResponse(Long roomId, boolean aiEnabled, ModerationAction actionMode) {
        public static SettingResponse from(ModerationSetting s) {
            return new SettingResponse(s.getRoom().getId(), s.isAiEnabled(), s.getActionMode());
        }
    }

    // `decision` must be APPROVED (keep as is), RESTORED (show again) or DELETED.
    public record ReviewRequest(@NotNull(message = "Choose approve, restore, or delete") ReviewStatus decision) {}

    public record FlaggedResponse(Long id, Long messageId, Long roomId, String senderName, String content,
                                  FlagReason reason, int confidence, FlagAction actionTaken,
                                  ReviewStatus reviewStatus, Instant flaggedAt, Instant reviewedAt) {
        public static FlaggedResponse from(FlaggedMessage f) {
            return new FlaggedResponse(f.getId(), f.getMessage().getId(), f.getMessage().getRoom().getId(),
                    f.getMessage().getSender().getDisplayName(), f.getMessage().getContent(), f.getReason(),
                    (int) Math.round(f.getConfidenceScore().doubleValue() * 100), f.getActionTaken(),
                    f.getReviewStatus(), f.getFlaggedAt(), f.getReviewedAt());
        }
    }
}
