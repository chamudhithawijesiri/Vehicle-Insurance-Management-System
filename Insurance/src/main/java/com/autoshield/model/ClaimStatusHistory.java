package com.autoshield.model;

import com.autoshield.enums.ClaimStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

// One row per decision change on a claim (Problem 1: admin decisions were
// being overwritten with no trace of what the previous decision was).
/**
 * Provides the ClaimStatusHistory functionality.
 */
@Entity
@Table(name = "claim_status_history")
public class ClaimStatusHistory {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The claim value. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false)
    private Claim claim;

    // Null only for the very first record, when the claim had no prior status change.
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private ClaimStatus previousStatus;
/** The newStatus value. */

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private ClaimStatus newStatus;

    // The officer/admin who made this decision. Nullable so existing claims
    // (changed before this feature existed) don't break if backfilled.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy;
/** The notes value. */

    @Column(length = 2000)
    private String notes;
/** The assessedAmount value. */

    @Column(name = "assessed_amount")
    private Double assessedAmount;
/** The changedAt value. */

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt = LocalDateTime.now();
/**
 * Executes the ClaimStatusHistory operation.
 */

    public ClaimStatusHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Claim getClaim() {
        return claim;
    }

    public void setClaim(Claim claim) {
        this.claim = claim;
    }

    public ClaimStatus getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(ClaimStatus previousStatus) {
        this.previousStatus = previousStatus;
    }

    public ClaimStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(ClaimStatus newStatus) {
        this.newStatus = newStatus;
    }

    public User getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Double getAssessedAmount() {
        return assessedAmount;
    }

    public void setAssessedAmount(Double assessedAmount) {
        this.assessedAmount = assessedAmount;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }
}