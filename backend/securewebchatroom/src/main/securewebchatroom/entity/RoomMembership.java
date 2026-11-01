package com.example.securewebchatroom.entity;

import com.example.securewebchatroom.enums.Role;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "room_memberships")
public class RoomMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role = Role.MEMBER;

    @Column(nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    public RoomMembership() {}

    public RoomMembership(ChatRoom room, User user, Role role) {
        this.room = room;
        this.user = user;
        this.role = role;
    }

    public boolean isOwner() { return role == Role.OWNER; }

    public Long getId() { return id; }
    public ChatRoom getRoom() { return room; }
    public User getUser() { return user; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Instant getJoinedAt() { return joinedAt; }
}
