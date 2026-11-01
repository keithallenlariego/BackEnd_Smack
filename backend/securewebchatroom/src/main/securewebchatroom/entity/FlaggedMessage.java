package com.example.securewebchatroom.entity;

import com.example.securewebchatroom.enums.FlagAction;
import com.example.securewebchatroom.enums.FlagReason;
import com.example.securewebchatroom.enums.ReviewStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "flagged_messages")
public class FlaggedMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "message_id", unique = true)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FlagReason reason;

    @Column(nullable = false, precision = 4, scale = 3)
    private BigDecimal confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private FlagAction actionTaken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant flaggedAt = Instant.now();

    private Instant reviewedAt;

    public FlaggedMessage() {}

    public FlaggedMessage(Message message, FlagReason reason, double confidence, FlagAction actionTaken) {
        this.message = message;
        this.reason = reason;
        this.confidenceScore = BigDecimal.valueOf(confidence).setScale(3, java.math.RoundingMode.HALF_UP);
        this.actionTaken = actionTaken;
    }

    public void review(ReviewStatus decision, User reviewer) {
        this.reviewStatus = decision;
        this.reviewedBy = reviewer;
        this.reviewedAt = Instant.now();
    }

    public Long getId() { return id; }
    public Message getMessage() { return message; }
    public FlagReason getReason() { return reason; }
    public BigDecimal getConfidenceScore() { return confidenceScore; }
    public FlagAction getActionTaken() { return actionTaken; }
    public ReviewStatus getReviewStatus() { return reviewStatus; }
    public Instant getFlaggedAt() { return flaggedAt; }
    public Instant getReviewedAt() { return reviewedAt; }
}
