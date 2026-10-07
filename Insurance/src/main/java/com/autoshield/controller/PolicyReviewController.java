package com.autoshield.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.service.ClaimService;
import com.autoshield.service.PolicyService;
import com.autoshield.service.UserService;
import com.autoshield.service.VehicleService;

/**
 * Officer/Manager side of Epic 2 & 3: reviewing and deciding on policy
 * applications and claims. Reachable by both ROLE_OFFICER and ROLE_ADMIN —
 * see SecurityConfig's /officer/** and /admin/claims rules.
 */
/**
 * Provides the PolicyReviewController functionality.
 */
@Controller
public class PolicyReviewController {
/** The policyService value. */

    private final PolicyService policyService;
/** The claimService value. */
    private final ClaimService claimService;
/** The userService value. */
    private final UserService userService;
/** The vehicleService value. */
    private final VehicleService vehicleService;
/**
 * Executes the PolicyReviewController operation.
 */

    public PolicyReviewController(PolicyService policyService, ClaimService claimService,
                                  UserService userService, VehicleService vehicleService) {
        this.policyService = policyService;
        this.claimService = claimService;
        this.userService = userService;
        this.vehicleService = vehicleService;
    }

    // The Officer's landing page after login — a quick-glance summary with
    // links in to the two review queues, instead of dumping them on the
    // customer-facing /packages page.
/**
 * Executes the officerDashboard operation.
 */
    @GetMapping("/officer/dashboard")
    public String officerDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        model.addAttribute("pendingPolicyCount", policyService.getPendingPolicies().size());
        model.addAttribute("totalPolicyCount", policyService.getAllPolicies().size());
        model.addAttribute("totalClaimCount", claimService.getAllClaims().size());
        return "officer/dashboard";
    }
/**
 * Executes the policyReviewQueue operation.
 */

    @GetMapping("/officer/policies")
    public String policyReviewQueue(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam(value = "view", required = false, defaultValue = "pending") String view,
                                    Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }

        List<Policy> policies = "all".equals(view) ? policyService.getAllPolicies() : policyService.getPendingPolicies();
        model.addAttribute("policies", policies);
        model.addAttribute("view", view);
        return "policy/officer-review";
    }
/**
 * Executes the approvePolicy operation.
 */

    @PostMapping("/officer/policies/{id}/approve")
    public String approvePolicy(@PathVariable("id") Long id,
                                @RequestParam(value = "reviewNotes", required = false) String reviewNotes,
                                RedirectAttributes redirectAttributes) {
        try {
            policyService.approvePolicy(id, reviewNotes);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }
        return "redirect:/officer/policies";
    }
/**
 * Executes the rejectPolicy operation.
 */

    @PostMapping("/officer/policies/{id}/reject")
    public String rejectPolicy(@PathVariable("id") Long id,
                               @RequestParam(value = "reviewNotes", required = false) String reviewNotes,
                               RedirectAttributes redirectAttributes) {
        try {
            policyService.rejectPolicy(id, reviewNotes);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("reviewError", e.getMessage());
        }
        return "redirect:/officer/policies";
    }

    // Shared "view customer" page, linked from both Policy Review and Claims
    // Review rows, so an officer can see a customer's full picture — other
    // vehicles, policies, and claim history — before deciding on the one in
    // front of them (Epic 2, US-11).
/**
 * Executes the customerDetail operation.
 */
    @GetMapping("/officer/customers/{id}")
    public String customerDetail(@PathVariable("id") Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        User customer = userService.getById(id);
        if (customer == null) {
            return "redirect:/officer/policies";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("vehicles", vehicleService.getVehiclesForUser(customer));
        model.addAttribute("policies", policyService.getPoliciesForUser(customer));
        model.addAttribute("claims", claimService.getClaimsForUser(customer));
        return "officer/customer-detail";
    }
}