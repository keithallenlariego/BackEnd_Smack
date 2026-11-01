package com.example.securewebchatroom.entity;

import com.example.securewebchatroom.enums.MessageStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id")
    private ChatRoom room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false, length = 2000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageStatus status = MessageStatus.VISIBLE;

    @Column(nullable = false, updatable = false)
    private Instant sentAt = Instant.now();

    public Message() {}

    public Message(ChatRoom room, User sender, String content) {
        this.room = room;
        this.sender = sender;
        this.content = content;
    }

    public Long getId() { return id; }
    public ChatRoom getRoom() { return room; }
    public User getSender() { return sender; }
    public String getContent() { return content; }
    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
    public Instant getSentAt() { return sentAt; }
}
