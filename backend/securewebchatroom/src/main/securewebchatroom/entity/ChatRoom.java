package com.example.securewebchatroom.entity;

import com.example.securewebchatroom.enums.Visibility;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false, length = 100)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility = Visibility.PRIVATE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ChatRoom() {}

    public ChatRoom(User owner, String name, String description, Visibility visibility) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.visibility = visibility;
    }

    public boolean isPrivate() { return visibility == Visibility.PRIVATE; }

    public Long getId() { return id; }
    public User getOwner() { return owner; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Visibility getVisibility() { return visibility; }
    public void setVisibility(Visibility visibility) { this.visibility = visibility; }
    public Instant getCreatedAt() { return createdAt; }
}
