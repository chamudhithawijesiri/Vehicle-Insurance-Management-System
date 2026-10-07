package com.autoshield.controller;

import com.autoshield.enums.PaymentStatus;
import com.autoshield.model.InsurancePlan;
import com.autoshield.service.InsurancePlanService;
import com.autoshield.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
/**
 * Provides the AdminController functionality.
 */

@Controller
public class AdminController {
/** The insurancePlanService value. */

    private final InsurancePlanService insurancePlanService;
/** The paymentService value. */
    private final PaymentService paymentService;
/**
 * Executes the AdminController operation.
 */

    public AdminController(InsurancePlanService insurancePlanService, PaymentService paymentService) {
        this.insurancePlanService = insurancePlanService;
        this.paymentService = paymentService;
    }

    // Admin: view/manage the insurance packages customers see on /packages.
/**
 * Executes the managePackages operation.
 */
    @GetMapping("/admin/packages")
    public String managePackages(Model model) {
        model.addAttribute("plans", insurancePlanService.getAllPlans());
        return "admin/packages";
    }

    // "coverage" is submitted as a comma-separated list of feature bullets,
    // e.g. "Accident damage, Theft, Fire" — matches how they're displayed
    // as a checklist on the customer packages page.
/**
 * Processes the createPackage operation.
 */
    @PostMapping("/admin/packages/create")
    public String createPackage(@RequestParam("planName") String planName,
                                @RequestParam("coverage") String coverage,
                                @RequestParam("basePrice") Double basePrice,
                                @RequestParam(value = "featured", required = false) Boolean featured,
                                Model model) {
        try {
            insurancePlanService.createPlan(planName, coverage, basePrice, Boolean.TRUE.equals(featured));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("plans", insurancePlanService.getAllPlans());
            return "admin/packages";
        }
        return "redirect:/admin/packages";
    }
/**
 * Processes the updatePackage operation.
 */

    @PostMapping("/admin/packages/{id}/update")
    public String updatePackage(@PathVariable("id") Long id,
                                @RequestParam("planName") String planName,
                                @RequestParam("coverage") String coverage,
                                @RequestParam("basePrice") Double basePrice,
                                @RequestParam(value = "featured", required = false) Boolean featured,
                                Model model) {
        try {
            insurancePlanService.updatePlan(id, planName, coverage, basePrice, Boolean.TRUE.equals(featured));
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("plans", insurancePlanService.getAllPlans());
            return "admin/packages";
        }
        return "redirect:/admin/packages";
    }
/**
 * Processes the deletePackage operation.
 */

    @PostMapping("/admin/packages/{id}/delete")
    public String deletePackage(@PathVariable("id") Long id, Model model) {
        try {
            insurancePlanService.deletePlan(id);
        } catch (IllegalStateException e) {
            // e.g. policies still reference this plan — show the message and
            // let the admin decide (edit it instead, or leave it as-is).
            model.addAttribute("error", e.getMessage());
            model.addAttribute("plans", insurancePlanService.getAllPlans());
            return "admin/packages";
        }
        return "redirect:/admin/packages";
    }

    // Admin: view payment history across every customer (Epic 4, US-09 /
    // dashboard revenue reporting). PaymentService already had everything
    // needed for this — only the controller and page were missing.
/**
 * Executes the paymentHistory operation.
 */
    @GetMapping("/admin/payments")
    public String paymentHistory(Model model) {
        model.addAttribute("payments", paymentService.getAllPayments());
        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        return "admin/payments";
    }

    // Lets the admin correct a payment's status directly from that same
    // page (e.g. marking a bounced bank transfer FAILED), reusing the exact
    // logic the Finance Officer's own view already relies on.
/**
 * Processes the updatePaymentStatus operation.
 */
    @PostMapping("/admin/payments/{id}/status")
    public String updatePaymentStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") PaymentStatus status) {
        paymentService.updateStatus(id, status);
        return "redirect:/admin/payments";
    }
}