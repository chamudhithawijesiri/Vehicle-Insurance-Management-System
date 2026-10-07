package com.autoshield.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoshield.enums.ClaimStatus;
import com.autoshield.model.Claim;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.service.ClaimService;
import com.autoshield.service.PolicyService;
import com.autoshield.service.UserService;
/**
 * Provides the ClaimController functionality.
 */

@Controller
public class ClaimController {
/** The claimService value. */

    private final ClaimService claimService;
/** The policyService value. */
    private final PolicyService policyService;
/** The userService value. */
    private final UserService userService;
/**
 * Executes the ClaimController operation.
 */

    public ClaimController(ClaimService claimService, PolicyService policyService, UserService userService) {
        this.claimService = claimService;
        this.policyService = policyService;
        this.userService = userService;
    }

    // ---------- Customer: submit a claim (US-01, US-02) ----------
/**
 * Processes the submitClaimForm operation.
 */

    @GetMapping("/claims/submit")
    public String submitClaimForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", user);

        List<Policy> policies = policyService.getPoliciesForUser(user);
        model.addAttribute("policies", policies);
        return "claim/submit-claim";
    }
/**
 * Processes the submitClaim operation.
 */

    @PostMapping("/claims/submit")
    public String submitClaim(@AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("policyId") Long policyId,
                              @RequestParam("incidentDate") String incidentDate,
                              @RequestParam("description") String description,
                              @RequestParam(value = "amountClaimed", required = false) Double amountClaimed,
                              @RequestParam(value = "policeReportFileName", required = false) String policeReportFileName,
                              @RequestParam(value = "policeReportFileData", required = false) String policeReportFileData,
                              @RequestParam(value = "vehicleImagesFileName", required = false) String vehicleImagesFileName,
                              @RequestParam(value = "vehicleImagesFileData", required = false) String vehicleImagesFileData,
                              @RequestParam(value = "garageQuotationFileName", required = false) String garageQuotationFileName,
                              @RequestParam(value = "garageQuotationFileData", required = false) String garageQuotationFileData,
                              @RequestParam(value = "nicCopyFileName", required = false) String nicCopyFileName,
                              @RequestParam(value = "nicCopyFileData", required = false) String nicCopyFileData,
                              Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        Policy policy = policyService.getById(policyId);

        // Ownership check: a customer can only file a claim against their own policy.
        if (policy == null || !policy.getVehicle().getOwner().getId().equals(user.getId())) {
            model.addAttribute("error", "Invalid policy selected.");
            model.addAttribute("currentUser", user);
            model.addAttribute("policies", policyService.getPoliciesForUser(user));
            return "claim/submit-claim";
        }

        LocalDate parsedIncidentDate;
        try {
            parsedIncidentDate = LocalDate.parse(incidentDate);
        } catch (DateTimeParseException | NullPointerException ex) {
            return submitClaimWithError(user, "Please enter a valid incident date.", model);
        }

        LocalDate today = LocalDate.now();
        if (parsedIncidentDate.isAfter(today)) {
            return submitClaimWithError(user, "The incident date cannot be in the future.", model);
        }
        if (policy.getStartDate() != null && parsedIncidentDate.isBefore(policy.getStartDate())) {
            return submitClaimWithError(user, "The incident date cannot be before the policy start date.", model);
        }
        if (policy.getExpiryDate() != null && parsedIncidentDate.isAfter(policy.getExpiryDate())) {
            return submitClaimWithError(user, "The incident date cannot be after the policy expiry date.", model);
        }

        Claim claim = claimService.submitClaim(policy, parsedIncidentDate, description, amountClaimed);

        claimService.attachDocument(claim, "Police Report", policeReportFileName, policeReportFileData);
        claimService.attachDocument(claim, "Vehicle Images", vehicleImagesFileName, vehicleImagesFileData);
        claimService.attachDocument(claim, "Garage Quotation", garageQuotationFileName, garageQuotationFileData);
        claimService.attachDocument(claim, "NIC Copy", nicCopyFileName, nicCopyFileData);

        return "redirect:/claims/" + claim.getId();
    }

    private String submitClaimWithError(User user, String error, Model model) {
        model.addAttribute("error", error);
        model.addAttribute("currentUser", user);
        model.addAttribute("policies", policyService.getPoliciesForUser(user));
        return "claim/submit-claim";
    }

    // ---------- Customer: track status + history (US-09, US-10) ----------
/**
 * Executes the claimHistory operation.
 */

    @GetMapping("/claims/history")
    public String claimHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", user);
        model.addAttribute("claims", claimService.getClaimsForUser(user));
        return "claim/claim-history";
    }
/**
 * Executes the claimDetails operation.
 */

    @GetMapping("/claims/{id}")
    public String claimDetails(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable("id") Long id,
                               Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        Claim claim = claimService.getById(id);

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // A customer may only view their own claim; an admin/officer can view any.
        if (claim == null || (!isAdmin && !claim.getPolicy().getVehicle().getOwner().getId().equals(user.getId()))) {
            return "redirect:/claims/history";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("claim", claim);
        return "claim/claim-details";
    }

    // ---------- Officer/Admin: review + decide (US-03 through US-08) ----------
/**
 * Executes the adminClaimList operation.
 */

    @GetMapping("/admin/claims")
    public String adminClaimList(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User admin = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", admin);
        }
        model.addAttribute("claims", claimService.getAllClaims());
        return "admin/claims";
    }
/**
 * Processes the updateClaimStatus operation.
 */

    @PostMapping("/admin/claims/{id}/status")
    public String updateClaimStatus(@AuthenticationPrincipal UserDetails userDetails,
                                    @PathVariable("id") Long id,
                                    @RequestParam("status") ClaimStatus status,
                                    @RequestParam(value = "assessmentNotes", required = false) String assessmentNotes,
                                    @RequestParam(value = "assessedAmount", required = false) Double assessedAmount) {
        User admin = userDetails != null ? userService.findByEmail(userDetails.getUsername()) : null;
        claimService.updateStatus(id, status, assessmentNotes, assessedAmount, admin);
        return "redirect:/admin/claims";
    }

    // ---------- Customer: upload more documents after submission (Problem 2) ----------
    // Reachable once the officer sets a claim to ADDITIONAL_DOCS_REQUIRED, since
    // the original submission form only allowed documents at claim creation time.
/**
 * Processes the addDocumentsForm operation.
 */

    @GetMapping("/claims/{id}/documents")
    public String addDocumentsForm(@AuthenticationPrincipal UserDetails userDetails,
                                   @PathVariable("id") Long id, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        Claim claim = claimService.getById(id);

        // Ownership check: a customer can only add documents to their own claim.
        if (claim == null || !claim.getPolicy().getVehicle().getOwner().getId().equals(user.getId())) {
            return "redirect:/claims/history";
        }

        model.addAttribute("currentUser", user);
        model.addAttribute("claim", claim);
        return "claim/add-documents";
    }
/**
 * Processes the addDocuments operation.
 */

    @PostMapping("/claims/{id}/documents")
    public String addDocuments(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable("id") Long id,
                               @RequestParam(value = "policeReportFileName", required = false) String policeReportFileName,
                               @RequestParam(value = "policeReportFileData", required = false) String policeReportFileData,
                               @RequestParam(value = "vehicleImagesFileName", required = false) String vehicleImagesFileName,
                               @RequestParam(value = "vehicleImagesFileData", required = false) String vehicleImagesFileData,
                               @RequestParam(value = "garageQuotationFileName", required = false) String garageQuotationFileName,
                               @RequestParam(value = "garageQuotationFileData", required = false) String garageQuotationFileData,
                               @RequestParam(value = "nicCopyFileName", required = false) String nicCopyFileName,
                               @RequestParam(value = "nicCopyFileData", required = false) String nicCopyFileData,
                               @RequestParam(value = "additionalDocumentFileName", required = false) String additionalDocumentFileName,
                               @RequestParam(value = "additionalDocumentFileData", required = false) String additionalDocumentFileData) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        Claim claim = claimService.getById(id);

        if (claim == null || !claim.getPolicy().getVehicle().getOwner().getId().equals(user.getId())) {
            return "redirect:/claims/history";
        }

        claimService.attachDocument(claim, "Police Report", policeReportFileName, policeReportFileData);
        claimService.attachDocument(claim, "Vehicle Images", vehicleImagesFileName, vehicleImagesFileData);
        claimService.attachDocument(claim, "Garage Quotation", garageQuotationFileName, garageQuotationFileData);
        claimService.attachDocument(claim, "NIC Copy", nicCopyFileName, nicCopyFileData);
        claimService.attachDocument(claim, "Additional Document", additionalDocumentFileName, additionalDocumentFileData);

        return "redirect:/claims/" + id;
    }
}