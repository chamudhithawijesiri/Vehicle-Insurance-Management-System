package com.autoshield.service;

import com.autoshield.enums.ClaimStatus;
import com.autoshield.model.Claim;
import com.autoshield.model.ClaimStatusHistory;
import com.autoshield.model.Policy;
import com.autoshield.model.User;

import java.time.LocalDate;
import java.util.List;
/**
 * Provides the ClaimService functionality.
 */

public interface ClaimService {

    // US-01/02: customer submits a claim, optionally with documents.
    // documentFiles maps documentType -> (fileName, base64 data) pairs handled by the controller.
/**
     * Executes the submitClaim operation.
     */
    Claim submitClaim(Policy policy, LocalDate incidentDate, String description, Double amountClaimed);

/**
     * Executes the attachDocument operation.
     */
    void attachDocument(Claim claim, String documentType, String fileName, String fileData);

    // US-09/10: customer tracks status / views history.
/**
     * Executes the getClaimsForUser operation.
     */
    List<Claim> getClaimsForUser(User user);

/**
     * Executes the getById operation.
     */
    Claim getById(Long id);

/**
     * Executes the getByClaimNumber operation.
     */
    Claim getByClaimNumber(String claimNumber);

    // Officer/Manager side (US-03 through US-08).
/**
     * Executes the getAllClaims operation.
     */
    List<Claim> getAllClaims();

    // Problem 1: changedBy records who made this decision, and a
    // ClaimStatusHistory row is written before the claim is overwritten so
    // the previous decision is never lost.
/**
     * Executes the updateStatus operation.
     */
    Claim updateStatus(Long claimId, ClaimStatus newStatus, String assessmentNotes, Double assessedAmount, User changedBy);

/**
     * Executes the getStatusHistory operation.
     */
    List<ClaimStatusHistory> getStatusHistory(Long claimId);

    // Epic 3, US-06: Vehicle Surveyor's own queue and report submission.
    // Kept separate from updateStatus()/history above — a survey doesn't
    // change the claim's status, it just records the surveyor's findings
    // for the Officer to use when they later approve/reject.
/**
     * Executes the getClaimsAwaitingSurvey operation.
     */
    List<Claim> getClaimsAwaitingSurvey();

/**
     * Executes the submitSurveyorReport operation.
     */
    Claim submitSurveyorReport(Long claimId, User surveyor, String report, Double assessedCost);

    // History view for the surveyor dashboard.
/**
     * Executes the getCompletedSurveys operation.
     */
    List<Claim> getCompletedSurveys();
}