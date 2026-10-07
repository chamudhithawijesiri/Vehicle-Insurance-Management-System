package com.autoshield.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.autoshield.enums.PaymentStatus;
import com.autoshield.model.Notification;
import com.autoshield.model.Payment;
import com.autoshield.model.User;
import com.autoshield.service.NotificationService;
import com.autoshield.service.PaymentService;
import com.autoshield.service.PolicyService;
import com.autoshield.service.UserService;

/**
 * Epic 4, US-04/US-05: the Finance Officer's own area for reconciling and
 * correcting payment records. Reachable by both ROLE_FINANCE and ROLE_ADMIN
 * — see SecurityConfig's /finance/** rule.
 */
/**
 * Provides the FinanceController functionality.
 */
@Controller
public class FinanceController {
/** The paymentService value. */

    private final PaymentService paymentService;
/** The userService value. */
    private final UserService userService;
/** The policyService value. */
    private final PolicyService policyService;
/** The notificationService value. */
    private final NotificationService notificationService;
/**
 * Executes the FinanceController operation.
 */

    public FinanceController(PaymentService paymentService, UserService userService, PolicyService policyService,
                             NotificationService notificationService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.policyService = policyService;
        this.notificationService = notificationService;
    }
/**
 * Executes the financeDashboard operation.
 */

    @GetMapping("/finance/dashboard")
    public String financeDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByEmail(userDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/login?error=true";
        }
        model.addAttribute("currentUser", currentUser);

        List<Payment> allPayments = paymentService.getAllPayments();
        long completedCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.COMPLETED).count();
        long pendingCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.PENDING).count();
        long failedCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.FAILED).count();
        long refundedCount = allPayments.stream().filter(p -> p.getStatus() == PaymentStatus.REFUNDED).count();

        model.addAttribute("totalPayments", allPayments.size());
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("failedCount", failedCount);
        model.addAttribute("refundedCount", refundedCount);
        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        model.addAttribute("notifications", notificationService.getForUser(currentUser));
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(currentUser));
        return "finance/dashboard";
    }
/**
 * Executes the financeNotifications check operation.
 */

    @GetMapping("/finance/notifications")
    @ResponseBody
    public Map<String, Object> financeNotifications(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return Map.of("notifications", List.of(), "unreadCount", 0);
        }
        User currentUser = userService.findByEmail(userDetails.getUsername());
        if (currentUser == null || !"ROLE_FINANCE".equals(currentUser.getRole())) {
            return Map.of("notifications", List.of(), "unreadCount", 0);
        }

        List<Notification> notifications = notificationService.getForUser(currentUser);
        List<Map<String, Object>> notificationItems = notifications.stream()
                .map(notification -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("title", notification.getTitle());
                    item.put("message", notification.getMessage() == null ? "" : notification.getMessage());
                    item.put("createdAt", notification.getCreatedAt().toString());
                    item.put("read", notification.isRead());
                    return item;
                })
                .collect(Collectors.toList());
        return Map.of(
                "notifications", notificationItems,
                "unreadCount", notificationService.getUnreadCount(currentUser));
    }
/**
 * Executes the approvePolicy operation.
 */

    @PostMapping("/finance/policies/{id}/approve")
    public String approvePolicy(@PathVariable("id") Long id,
                                @RequestParam(value = "reviewNotes", required = false) String reviewNotes) {
        policyService.approvePolicy(id, reviewNotes);
        return "redirect:/finance/dashboard";
    }
/**
 * Executes the rejectPolicy operation.
 */

    @PostMapping("/finance/policies/{id}/reject")
    public String rejectPolicy(@PathVariable("id") Long id,
                               @RequestParam(value = "reviewNotes", required = false) String reviewNotes) {
        policyService.rejectPolicy(id, reviewNotes);
        return "redirect:/finance/dashboard";
    }
/**
 * Executes the paymentRecords operation.
 */

    @GetMapping("/finance/payments")
    public String paymentRecords(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(required = false) PaymentStatus status,
                                 Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        List<Payment> payments = paymentService.getAllPayments();
        if (status != null) {
            payments = payments.stream()
                    .filter(payment -> payment.getStatus() == status)
                    .toList();
        }
        model.addAttribute("payments", payments);
        model.addAttribute("selectedStatus", status);
        return "finance/payments";
    }
/**
 * Executes the paymentHistory operation.
 */

    @GetMapping("/finance/payment-history")
    public String paymentHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User currentUser = userService.findByEmail(userDetails.getUsername());
        if (currentUser == null) {
            return "redirect:/login?error=true";
        }
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("payments", paymentService.getAllPayments());
        return "finance/payment-history";
    }
/**
 * Processes the updatePaymentStatus operation.
 */

    @PostMapping("/finance/payments/{id}/status")
    public String updatePaymentStatus(@PathVariable("id") Long id,
                                      @RequestParam("status") PaymentStatus status,
                                      @RequestParam(required = false) PaymentStatus returnStatus) {
        paymentService.updateStatus(id, status);
        return returnStatus == null
                ? "redirect:/finance/payments"
                : "redirect:/finance/payments?status=" + returnStatus.name();
    }
}