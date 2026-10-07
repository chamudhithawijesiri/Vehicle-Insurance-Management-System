package com.autoshield.model;

import com.autoshield.enums.NotificationType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
/**
 * Provides the Notification functionality.
 */

@Entity
@Table(name = "notifications")
public class Notification {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The customer this notification is for.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
/** The type value. */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
/** The title value. */

    @Column(nullable = false)
    private String title;
/** The message value. */

    @Column(length = 1000)
    private String message;
/** The isRead value. */

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;
/** The createdAt value. */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
/**
 * Executes the Notification operation.
 */

    public Notification() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}