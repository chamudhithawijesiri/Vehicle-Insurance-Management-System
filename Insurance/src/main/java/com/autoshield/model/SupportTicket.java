package com.autoshield.model;

import com.autoshield.enums.TicketCategory;
import com.autoshield.enums.TicketPriority;
import com.autoshield.enums.TicketStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Provides the SupportTicket functionality.
 */

@Entity
@Table(name = "support_tickets")
public class SupportTicket {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The ticketNumber value. */

    @Column(name = "ticket_number", nullable = false, unique = true)
    private String ticketNumber;

    // The customer who raised the request (Epic 5, US-01).
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;
/** The subject value. */

    @Column(nullable = false)
    private String subject;
/** The category value. */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;
/** The description value. */

    @Column(length = 2000, nullable = false)
    private String description;

    // Free-text pointer to a related policy/claim/payment number, if any (US-01).
    @Column(name = "reference_info")
    private String referenceInfo;
/** The priority value. */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority = TicketPriority.MEDIUM;
/** The status value. */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status = TicketStatus.NEW;

    // The Customer Service Officer currently handling this ticket (US-05).
    // In this system, "Customer Service Officer" is played by a ROLE_ADMIN
    // account, per the epic's own note that "customer service office = ADMIN".
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private User assignedTo;
/** The createdAt value. */

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
/** The updatedAt value. */

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
/** The resolvedAt value. */

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
/** The responses value. */

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupportTicketResponse> responses = new ArrayList<>();
/**
 * Executes the SupportTicket operation.
 */

    public SupportTicket() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceInfo() {
        return referenceInfo;
    }

    public void setReferenceInfo(String referenceInfo) {
        this.referenceInfo = referenceInfo;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public List<SupportTicketResponse> getResponses() {
        return responses;
    }

    public void setResponses(List<SupportTicketResponse> responses) {
        this.responses = responses;
    }
}