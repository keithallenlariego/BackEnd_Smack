package com.example.securewebchatroom.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    private String displayName;
    private String bio;

    @Column(nullable = false)
    private String dashboardTheme = "clay";

    @Column(nullable = false)
    private String defaultView = "grid";

    @Column(nullable = false)
    private boolean showFlagCount = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public User() {}

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.displayName = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getDashboardTheme() { return dashboardTheme; }
    public void setDashboardTheme(String dashboardTheme) { this.dashboardTheme = dashboardTheme; }
    public String getDefaultView() { return defaultView; }
    public void setDefaultView(String defaultView) { this.defaultView = defaultView; }
    public boolean isShowFlagCount() { return showFlagCount; }
    public void setShowFlagCount(boolean showFlagCount) { this.showFlagCount = showFlagCount; }
    public Instant getCreatedAt() { return createdAt; }
}
