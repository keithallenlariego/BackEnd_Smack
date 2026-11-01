package com.example.securewebchatroom.entity;

import com.example.securewebchatroom.enums.ModerationAction;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "moderation_settings")
public class ModerationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "room_id", unique = true)
    private ChatRoom room;

    @Column(nullable = false)
    private boolean aiEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ModerationAction actionMode = ModerationAction.HIDE;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public ModerationSetting() {}

    public ModerationSetting(ChatRoom room, boolean aiEnabled) {
        this.room = room;
        this.aiEnabled = aiEnabled;
    }

    public Long getId() { return id; }
    public ChatRoom getRoom() { return room; }
    public boolean isAiEnabled() { return aiEnabled; }
    public ModerationAction getActionMode() { return actionMode; }

    public void update(boolean aiEnabled, ModerationAction actionMode) {
        this.aiEnabled = aiEnabled;
        this.actionMode = actionMode;
        this.updatedAt = Instant.now();
    }
}
