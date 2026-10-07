package com.autoshield.model;

import jakarta.persistence.*;
/**
 * Provides the InsurancePlan functionality.
 */

@Entity
@Table(name = "insurance_plans")
public class InsurancePlan {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The planName value. */

    @Column(name = "plan_name", nullable = false, unique = true)
    private String planName; // e.g. "Motor Third Party", "Motor Comprehensive"
/** The coverage value. */

    @Column(name = "coverage", length = 1000)
    private String coverage;

    // Base price used by the premium calculator before vehicle-specific adjustments
    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    // Marks the one plan shown with the highlighted "featured" border on the
    // customer packages page (admin-controlled, at most one should be true).
    @Column(name = "featured", nullable = false)
    private boolean featured = false;
/**
 * Executes the InsurancePlan operation.
 */

    public InsurancePlan() {
    }
/**
 * Executes the InsurancePlan operation.
 */

    public InsurancePlan(String planName, String coverage, Double basePrice) {
        this.planName = planName;
        this.coverage = coverage;
        this.basePrice = basePrice;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public Double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Double basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }
}