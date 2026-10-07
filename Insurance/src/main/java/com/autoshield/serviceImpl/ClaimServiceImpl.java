package com.autoshield.serviceImpl;

import com.autoshield.enums.ClaimStatus;
import com.autoshield.enums.NotificationType;
import com.autoshield.model.Claim;
import com.autoshield.model.ClaimDocument;
import com.autoshield.model.ClaimStatusHistory;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.repository.ClaimDocumentRepository;
import com.autoshield.repository.ClaimRepository;
import com.autoshield.repository.ClaimStatusHistoryRepository;
import com.autoshield.service.ClaimService;
import com.autoshield.service.NotificationService;
import com.autoshield.util.ClaimNumberGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Provides the ClaimServiceImpl functionality.
 */

@Service
public class ClaimServiceImpl implements ClaimService {
/** The claimRepository value. */

    private final ClaimRepository claimRepository;
/** The claimDocumentRepository value. */
    private final ClaimDocumentRepository claimDocumentRepository;
/** The claimStatusHistoryRepository value. */
    private final ClaimStatusHistoryRepository claimStatusHistoryRepository;
/** The claimNumberGenerator value. */
    private final ClaimNumberGenerator claimNumberGenerator;
/** The notificationService value. */
    private final NotificationService notificationService;
/**
 * Executes the ClaimServiceImpl operation.
 */

    public ClaimServiceImpl(ClaimRepository claimRepository,
                            ClaimDocumentRepository claimDocumentRepository,
                            ClaimStatusHistoryRepository claimStatusHistoryRepository,
                            ClaimNumberGenerator claimNumberGenerator,
                            NotificationService notificationService) {
        this.claimRepository = claimRepository;
        this.claimDocumentRepository = claimDocumentRepository;
        this.claimStatusHistoryRepository = claimStatusHistoryRepository;
        this.claimNumberGenerator = claimNumberGenerator;
        this.notificationService = notificationService;
    }
/**
 * Processes the submitClaim operation.
 */

    @Override
    public Claim submitClaim(Policy policy, LocalDate incidentDate, String description, Double amountClaimed) {
        Claim claim = new Claim();
        claim.setClaimNumber(claimNumberGenerator.generate());
        claim.setPolicy(policy);
        claim.setIncidentDate(incidentDate);
        claim.setDescription(description);
        claim.setAmountClaimed(amountClaimed);
        claim.setStatus(ClaimStatus.SUBMITTED);
        return claimRepository.save(claim);
    }
/**
 * Processes the attachDocument operation.
 */

    @Override
    public void attachDocument(Claim claim, String documentType, String fileName, String fileData) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        ClaimDocument doc = new ClaimDocument();
        doc.setClaim(claim);
        doc.setDocumentType(documentType);
        doc.setFileName(fileName);
        doc.setFileData(fileData);
        claimDocumentRepository.save(doc);
    }

    @Override
    public List<Claim> getClaimsForUser(User user) {
        return claimRepository.findByPolicy_Vehicle_Owner(user);
    }

    @Override
    public Claim getById(Long id) {
        return claimRepository.findById(id).orElse(null);
    }

    @Override
    public Claim getByClaimNumber(String claimNumber) {
        return claimRepository.findByClaimNumber(claimNumber).orElse(null);
    }

    @Override
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }
/**
 * Processes the updateStatus operation.
 */

    @Override
    public Claim updateStatus(Long claimId, ClaimStatus newStatus, String assessmentNotes, Double assessedAmount, User changedBy) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));

        // Problem 1: record what the decision *was* before we overwrite it,
        // so the admin can always look back at the full trail of decisions.
        ClaimStatusHistory history = new ClaimStatusHistory();
        history.setClaim(claim);
        history.setPreviousStatus(claim.getStatus());
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setNotes(assessmentNotes);
        history.setAssessedAmount(assessedAmount);
        claimStatusHistoryRepository.save(history);

        claim.setStatus(newStatus);
        if (assessmentNotes != null && !assessmentNotes.isBlank()) {
            claim.setAssessmentNotes(assessmentNotes);
        }
        if (assessedAmount != null) {
            claim.setAssessedAmount(assessedAmount);
        }
        Claim saved = claimRepository.save(claim);

        // Epic 4, US-10: notify the customer whenever their claim's status changes.
        User owner = claim.getPolicy().getVehicle().getOwner();
        String statusLabel = switch (newStatus) {
            case UNDER_REVIEW -> "is now under review";
            case ADDITIONAL_DOCS_REQUIRED -> "needs additional documents — please check the claim details";
            case APPROVED -> "has been approved";
            case REJECTED -> "was rejected";
            case COMPLETED -> "has been completed";
            default -> "was updated";
        };
        notificationService.notify(
                owner,
                NotificationType.CLAIM_STATUS,
                "Claim " + claim.getClaimNumber() + " " + statusLabel,
                assessmentNotes != null && !assessmentNotes.isBlank() ? "Officer notes: " + assessmentNotes : null
        );

        return saved;
    }

    @Override
    public List<ClaimStatusHistory> getStatusHistory(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));
        return claimStatusHistoryRepository.findByClaimOrderByChangedAtDesc(claim);
    }

    @Override
    public List<Claim> getClaimsAwaitingSurvey() {
        return claimRepository.findByStatusAndSurveyorReportIsNull(ClaimStatus.UNDER_REVIEW);
    }
/**
 * Processes the submitSurveyorReport operation.
 */

    @Override
    public Claim submitSurveyorReport(Long claimId, User surveyor, String report, Double assessedCost) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));
        claim.setSurveyorReport(report);
        claim.setSurveyorAssessedCost(assessedCost);
        claim.setSurveyedBy(surveyor);
        claim.setSurveyedAt(LocalDateTime.now());
        Claim saved = claimRepository.save(claim);

        // Let the officer handling this claim know the assessment is in —
        // the customer isn't notified here since the claim's own status
        // hasn't changed yet (the officer still needs to make the decision).
        User owner = claim.getPolicy().getVehicle().getOwner();
        notificationService.notify(
                owner,
                NotificationType.CLAIM_STATUS,
                "Claim " + claim.getClaimNumber() + " has been inspected",
                "A vehicle surveyor has completed the damage assessment for your claim."
        );

        return saved;
    }

    @Override
    public List<Claim> getCompletedSurveys() {
        return claimRepository.findBySurveyorReportIsNotNullOrderBySurveyedAtDesc();
    }
}