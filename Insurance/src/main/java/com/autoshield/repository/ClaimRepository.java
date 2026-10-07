package com.autoshield.repository;

import com.autoshield.enums.ClaimStatus;
import com.autoshield.model.Claim;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Provides the ClaimRepository functionality.
 */

public interface ClaimRepository extends JpaRepository<Claim, Long> {
/**
     * Executes the findByClaimNumber operation.
     */
    Optional<Claim> findByClaimNumber(String claimNumber);
/**
     * Executes the findByPolicy operation.
     */
    List<Claim> findByPolicy(Policy policy);

    // Every claim filed by a given customer, across all their vehicles/policies.
/**
     * Executes the findByPolicy_Vehicle_Owner operation.
     */
    List<Claim> findByPolicy_Vehicle_Owner(User owner);

    // Epic 3, US-06: claims under review that a Vehicle Surveyor hasn't
    // assessed yet.
/**
     * Executes the findByStatusAndSurveyorReportIsNull operation.
     */
    List<Claim> findByStatusAndSurveyorReportIsNull(ClaimStatus status);

    // History view: every claim a surveyor HAS already reported on, newest first.
/**
     * Executes the findBySurveyorReportIsNotNullOrderBySurveyedAtDesc operation.
     */
    List<Claim> findBySurveyorReportIsNotNullOrderBySurveyedAtDesc();
}