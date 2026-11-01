package com.example.securewebchatroom.service;

import com.example.securewebchatroom.dto.MessageDto.MessageResponse;
import com.example.securewebchatroom.dto.ModerationDto.FlaggedResponse;
import com.example.securewebchatroom.dto.ModerationDto.SettingRequest;
import com.example.securewebchatroom.dto.ModerationDto.SettingResponse;
import com.example.securewebchatroom.entity.FlaggedMessage;
import com.example.securewebchatroom.entity.Message;
import com.example.securewebchatroom.entity.ModerationSetting;
import com.example.securewebchatroom.enums.FlagAction;
import com.example.securewebchatroom.enums.MessageStatus;
import com.example.securewebchatroom.enums.ReviewStatus;
import com.example.securewebchatroom.exception.ApiException;
import com.example.securewebchatroom.repository.FlaggedMessageRepository;
import com.example.securewebchatroom.repository.MessageRepository;
import com.example.securewebchatroom.repository.ModerationSettingRepository;
import com.example.securewebchatroom.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModerationService {

    private final ModerationSettingRepository settings;
    private final FlaggedMessageRepository flagged;
    private final MessageRepository messages;
    private final UserRepository users;
    private final MembershipService access;
    private final ContentAnalyzer analyzer;
    private final SimpMessagingTemplate broker;

    public ModerationService(ModerationSettingRepository settings, FlaggedMessageRepository flagged, MessageRepository messages,
                             UserRepository users, MembershipService access, ContentAnalyzer analyzer, SimpMessagingTemplate broker) {
        this.settings = settings;
        this.flagged = flagged;
        this.messages = messages;
        this.users = users;
        this.access = access;
        this.analyzer = analyzer;
        this.broker = broker;
    }

    // ----- owner settings -----

    @Transactional(readOnly = true)
    public SettingResponse getSettings(Long roomId, Long userId) {
        access.requireOwner(roomId, userId);
        return SettingResponse.from(findSettings(roomId));
    }

    @Transactional
    public SettingResponse updateSettings(Long roomId, Long userId, SettingRequest request) {
        access.requireOwner(roomId, userId);
        ModerationSetting setting = findSettings(roomId);
        setting.update(request.aiEnabled(), request.actionMode());
        return SettingResponse.from(settings.save(setting));
    }

    // ----- called by MessageService for every new message -----

    // Checks the message when the AI moderator is on. Returns a note for the sender, or null.
    @Transactional
    public String moderate(Message message) {
        Long roomId = message.getRoom().getId();
        ModerationSetting setting = settings.findByRoomId(roomId).orElse(null);
        if (setting == null || !setting.isAiEnabled()) return null;

        ContentAnalyzer.Detection detection = analyzer.analyze(message.getContent());
        if (detection == null) return null;

        FlagAction action = switch (setting.getActionMode()) {
            case WARN -> FlagAction.WARNED;
            case HIDE -> FlagAction.HIDDEN;
            case NOTIFY -> FlagAction.NOTIFIED;
        };
        if (action == FlagAction.HIDDEN) {
            message.setStatus(MessageStatus.HIDDEN);
            messages.save(message);
        }
        FlaggedMessage flag = flagged.save(new FlaggedMessage(message, detection.reason(), detection.confidence(), action));

        // Live notice for the owner (only the owner may subscribe to this topic).
        broker.convertAndSend("/topic/rooms/" + roomId + "/moderation", FlaggedResponse.from(flag));

        String reason = detection.reason().name().toLowerCase();
        return switch (action) {
            case WARNED -> "Your message looks like " + reason + ". Please keep the room friendly.";
            case HIDDEN -> "Your message was hidden because it looks like " + reason + ". The room owner will review it.";
            case NOTIFIED -> null; // the sender is not told; the owner is notified
        };
    }

    // ----- owner review -----

    @Transactional(readOnly = true)
    public List<FlaggedResponse> listFlagged(Long roomId, Long userId) {
        access.requireOwner(roomId, userId);
        return flagged.findByMessageRoomIdOrderByFlaggedAtDesc(roomId).stream().map(FlaggedResponse::from).toList();
    }

    // APPROVED = the owner agrees with the AI (the message stays as it is),
    // RESTORED = show the message to the room again, DELETED = remove it.
    @Transactional
    public FlaggedResponse review(Long flagId, Long userId, ReviewStatus decision) {
        if (decision == ReviewStatus.PENDING) throw ApiException.badRequest("Choose approve, restore, or delete");
        FlaggedMessage flag = flagged.findById(flagId).orElseThrow(() -> ApiException.notFound("Flagged message not found"));
        Message message = flag.getMessage();
        Long roomId = message.getRoom().getId();
        access.requireOwner(roomId, userId);
        if (flag.getReviewStatus() != ReviewStatus.PENDING) throw ApiException.conflict("This message was already reviewed");

        if (decision == ReviewStatus.RESTORED) {
            message.setStatus(MessageStatus.VISIBLE);
            messages.save(message);
            broker.convertAndSend("/topic/rooms/" + roomId, MessageResponse.from(message, null)); // everyone sees it now
        } else if (decision == ReviewStatus.DELETED) {
            message.setStatus(MessageStatus.DELETED);
            messages.save(message);
        }
        flag.review(decision, users.getReferenceById(userId));
        return FlaggedResponse.from(flagged.save(flag));
    }

    private ModerationSetting findSettings(Long roomId) {
        return settings.findByRoomId(roomId).orElseThrow(() -> ApiException.notFound("Room not found"));
    }
}
