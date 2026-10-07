package com.autoshield.model;

import com.autoshield.enums.PolicyStatus;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
/**
 * Provides the Policy functionality.
 */

@Entity
@Table(name = "policies")
public class Policy {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The policyNumber value. */

    @Column(name = "policy_number", nullable = false, unique = true)
    private String policyNumber;
/** The vehicle value. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
/** The plan value. */

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private InsurancePlan plan;
/** The premium value. */

    @Column(name = "premium", nullable = false)
    private Double premium;
/** The startDate value. */

    @Column(name = "start_date")
    private LocalDate startDate;
/** The expiryDate value. */

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
/** The status value. */

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "varchar(20)")
    private PolicyStatus status = PolicyStatus.PENDING;
/** The reviewNotes value. */

    @Column(name = "review_notes", length = 1000)
    private String reviewNotes;

    // --- Vehicle Surveyor's pre-approval inspection (Epic 2, US-06) ---
    @Column(name = "inspection_report", length = 2000)
    private String inspectionReport;
/** The inspectionPassed value. */

    @Column(name = "inspection_passed")
    private Boolean inspectionPassed;
/** The inspectedBy value. */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspected_by_user_id")
    private User inspectedBy;
/** The inspectedAt value. */

    @Column(name = "inspected_at")
    private LocalDateTime inspectedAt;
/**
 * Executes the Policy operation.
 */

    public Policy() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public InsurancePlan getPlan() {
        return plan;
    }

    public void setPlan(InsurancePlan plan) {
        this.plan = plan;
    }

    public Double getPremium() {
        return premium;
    }

    public void setPremium(Double premium) {
        this.premium = premium;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public void setReviewNotes(String reviewNotes) {
        this.reviewNotes = reviewNotes;
    }

    public String getInspectionReport() {
        return inspectionReport;
    }

    public void setInspectionReport(String inspectionReport) {
        this.inspectionReport = inspectionReport;
    }

    public Boolean getInspectionPassed() {
        return inspectionPassed;
    }

    public void setInspectionPassed(Boolean inspectionPassed) {
        this.inspectionPassed = inspectionPassed;
    }

    public User getInspectedBy() {
        return inspectedBy;
    }

    public void setInspectedBy(User inspectedBy) {
        this.inspectedBy = inspectedBy;
    }

    public LocalDateTime getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(LocalDateTime inspectedAt) {
        this.inspectedAt = inspectedAt;
    }
}