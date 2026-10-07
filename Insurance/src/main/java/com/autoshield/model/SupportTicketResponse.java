package com.autoshield.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

// One message in a ticket's conversation thread - either the customer
// or the handling officer/admin can post one (Epic 5, US-05/US-08).
/**
 * Provides the SupportTicketResponse functionality.
 */
@Entity
@Table(name = "support_ticket_responses")
public class SupportTicketResponse {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The ticket value. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;
/** The author value. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // true when posted by the Customer Service Officer/Admin, false when posted
    // by the customer. Lets the template style the two sides differently.
    @Column(name = "from_staff", nullable = false)
    private boolean fromStaff;
/** The message value. */

    @Column(length = 2000, nullable = false)
    private String message;

    // US-08: officer marks a message as an information request, so the ticket
    // can be flagged "Waiting for Customer" until the customer replies.
    @Column(name = "info_requested", nullable = false)
    private boolean infoRequested = false;
/** The createdAt value. */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
/**
 * Executes the SupportTicketResponse operation.
 */

    public SupportTicketResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SupportTicket getTicket() {
        return ticket;
    }

    public void setTicket(SupportTicket ticket) {
        this.ticket = ticket;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public boolean isFromStaff() {
        return fromStaff;
    }

    public void setFromStaff(boolean fromStaff) {
        this.fromStaff = fromStaff;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isInfoRequested() {
        return infoRequested;
    }

    public void setInfoRequested(boolean infoRequested) {
        this.infoRequested = infoRequested;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}