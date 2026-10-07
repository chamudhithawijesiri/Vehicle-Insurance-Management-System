package com.autoshield.model;

import com.autoshield.enums.ClaimStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Provides the Claim functionality.
 */

@Entity
@Table(name = "claims")
public class Claim {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The claimNumber value. */

    @Column(name = "claim_number", nullable = false, unique = true)
    private String claimNumber;

    // The policy this claim is filed against. From the policy we can reach
    // both the vehicle and the customer, so we don't duplicate those here.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
/** The incidentDate value. */

    @Column(name = "incident_date", nullable = false)
    private LocalDate incidentDate;
/** The description value. */

    @Column(name = "description", length = 2000, nullable = false)
    private String description;
/** The amountClaimed value. */

    @Column(name = "amount_claimed")
    private Double amountClaimed;

    // Filled in later by a Claims Officer / Vehicle Surveyor as the claim moves
    // through review (Epic 3, US-04 through US-07).
    @Column(name = "assessment_notes", length = 2000)
    private String assessmentNotes;
/** The assessedAmount value. */

    @Column(name = "assessed_amount")
    private Double assessedAmount;
/** The status value. */

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status = ClaimStatus.SUBMITTED;
/** The submittedAt value. */

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    // Filled in by a Vehicle Surveyor during physical damage assessment
    // (Epic 3, US-06) — kept separate from assessmentNotes/assessedAmount
    // above, which are the Officer's own decision fields.
    @Column(name = "surveyor_report", length = 2000)
    private String surveyorReport;
/** The surveyorAssessedCost value. */

    @Column(name = "surveyor_assessed_cost")
    private Double surveyorAssessedCost;
/** The surveyedBy value. */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surveyed_by")
    private User surveyedBy;
/** The surveyedAt value. */

    @Column(name = "surveyed_at")
    private LocalDateTime surveyedAt;
/** The documents value. */

    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClaimDocument> documents = new ArrayList<>();

    // Problem 1: full audit trail of every decision made on this claim,
    // always loaded newest-first.
    @OneToMany(mappedBy = "claim", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt DESC")
    private List<ClaimStatusHistory> statusHistory = new ArrayList<>();
/**
 * Executes the Claim operation.
 */

    public Claim() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public LocalDate getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDate incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAmountClaimed() {
        return amountClaimed;
    }

    public void setAmountClaimed(Double amountClaimed) {
        this.amountClaimed = amountClaimed;
    }

    public String getAssessmentNotes() {
        return assessmentNotes;
    }

    public void setAssessmentNotes(String assessmentNotes) {
        this.assessmentNotes = assessmentNotes;
    }

    public Double getAssessedAmount() {
        return assessedAmount;
    }

    public void setAssessedAmount(Double assessedAmount) {
        this.assessedAmount = assessedAmount;
    }

    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public String getSurveyorReport() {
        return surveyorReport;
    }

    public void setSurveyorReport(String surveyorReport) {
        this.surveyorReport = surveyorReport;
    }

    public Double getSurveyorAssessedCost() {
        return surveyorAssessedCost;
    }

    public void setSurveyorAssessedCost(Double surveyorAssessedCost) {
        this.surveyorAssessedCost = surveyorAssessedCost;
    }

    public User getSurveyedBy() {
        return surveyedBy;
    }

    public void setSurveyedBy(User surveyedBy) {
        this.surveyedBy = surveyedBy;
    }

    public LocalDateTime getSurveyedAt() {
        return surveyedAt;
    }

    public void setSurveyedAt(LocalDateTime surveyedAt) {
        this.surveyedAt = surveyedAt;
    }

    public List<ClaimDocument> getDocuments() {
        return documents;
    }

    public void setDocuments(List<ClaimDocument> documents) {
        this.documents = documents;
    }

    public List<ClaimStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public void setStatusHistory(List<ClaimStatusHistory> statusHistory) {
        this.statusHistory = statusHistory;
    }
}