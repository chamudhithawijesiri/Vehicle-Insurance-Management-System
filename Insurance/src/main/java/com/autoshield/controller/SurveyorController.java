package com.autoshield.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoshield.model.Claim;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.service.ClaimService;
import com.autoshield.service.PolicyService;
import com.autoshield.service.UserService;

/**
 * The Vehicle Surveyor's own narrow area — two separate queues:
 *  - Epic 3, US-06: claims that need a physical damage assessment.
 *  - Epic 2, US-06: policy applications that need a pre-approval vehicle
 *    inspection.
 * In both cases the surveyor only records findings — the actual approve/
 * reject decision stays with the Officer.
 */
/**
 * Provides the SurveyorController functionality.
 */
@Controller
public class SurveyorController {
/** The claimService value. */

    private final ClaimService claimService;
/** The policyService value. */
    private final PolicyService policyService;
/** The userService value. */
    private final UserService userService;
/**
 * Executes the SurveyorController operation.
 */

    public SurveyorController(ClaimService claimService, PolicyService policyService, UserService userService) {
        this.claimService = claimService;
        this.policyService = policyService;
        this.userService = userService;
    }
/**
 * Executes the surveyorDashboard operation.
 */

    @GetMapping("/surveyor/dashboard")
    public String surveyorDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("pendingSurveyCount", claimService.getClaimsAwaitingSurvey().size());
        model.addAttribute("pendingInspectionCount", policyService.getPoliciesAwaitingInspection().size());
        return "surveyor/dashboard";
    }

    // ---- Claims damage assessment (Epic 3, US-06) ----
/**
 * Executes the surveyQueue operation.
 */

    @GetMapping("/surveyor/claims")
    public String surveyQueue(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("claims", claimService.getClaimsAwaitingSurvey());
        return "surveyor/claims";
    }
/**
 * Executes the assessmentForm operation.
 */

    @GetMapping("/surveyor/claims/{id}")
    public String assessmentForm(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        Claim claim = claimService.getById(id);
        if (claim == null) {
            return "redirect:/surveyor/claims";
        }
        model.addAttribute("claim", claim);
        return "surveyor/claim-assessment";
    }
/**
 * Processes the submitAssessment operation.
 */

    @PostMapping("/surveyor/claims/{id}")
    public String submitAssessment(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam("report") String report,
                                   @RequestParam(value = "assessedCost", required = false) Double assessedCost) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User surveyor = userService.findByEmail(userDetails.getUsername());
        claimService.submitSurveyorReport(id, surveyor, report, assessedCost);
        return "redirect:/surveyor/claims";
    }
/**
 * Executes the claimHistory operation.
 */

    @GetMapping("/surveyor/claims/history")
    public String claimHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("claims", claimService.getCompletedSurveys());
        return "surveyor/claims-history";
    }

    // ---- Vehicle inspection for policy applications (Epic 2, US-06) ----
/**
 * Executes the inspectionQueue operation.
 */

    @GetMapping("/surveyor/policies")
    public String inspectionQueue(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("policies", policyService.getPoliciesAwaitingInspection());
        return "surveyor/policies";
    }
/**
 * Executes the inspectionForm operation.
 */

    @GetMapping("/surveyor/policies/{id}")
    public String inspectionForm(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        Policy policy = policyService.getById(id);
        if (policy == null) {
            return "redirect:/surveyor/policies";
        }
        model.addAttribute("policy", policy);
        return "surveyor/vehicle-inspection";
    }
/**
 * Processes the submitInspection operation.
 */

    @PostMapping("/surveyor/policies/{id}")
    public String submitInspection(@PathVariable("id") Long id,
                                   @AuthenticationPrincipal UserDetails userDetails,
                                   @RequestParam("report") String report,
                                   @RequestParam("passed") boolean passed) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User surveyor = userService.findByEmail(userDetails.getUsername());
        policyService.submitVehicleInspection(id, surveyor, report, passed);
        return "redirect:/surveyor/policies";
    }
/**
 * Executes the policyHistory operation.
 */

    @GetMapping("/surveyor/policies/history")
    public String policyHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("policies", policyService.getCompletedInspections());
        return "surveyor/policies-history";
    }
}