package com.autoshield.controller;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.autoshield.model.Payment;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.model.Vehicle;
import com.autoshield.service.ClaimService;
import com.autoshield.service.InsurancePlanService;
import com.autoshield.service.NotificationService;
import com.autoshield.service.PaymentService;
import com.autoshield.service.PolicyService;
import com.autoshield.service.UserService;
import com.autoshield.service.VehicleService;
/**
 * Provides the AuthController functionality.
 */

@Controller
public class AuthController {

    private static final Pattern BANK_TRANSACTION_REFERENCE =
            Pattern.compile("^[A-Z]{3}[0-9]{11}$");
/** The userService value. */

    private final UserService userService;
/** The vehicleService value. */
    private final VehicleService vehicleService;
/** The policyService value. */
    private final PolicyService policyService;
/** The claimService value. */
    private final ClaimService claimService;
/** The insurancePlanService value. */
    private final InsurancePlanService insurancePlanService;
/** The paymentService value. */
    private final PaymentService paymentService;
/** The notificationService value. */
    private final NotificationService notificationService;
/**
 * Executes the AuthController operation.
 */

    public AuthController(UserService userService, VehicleService vehicleService,
                          PolicyService policyService, ClaimService claimService,
                          InsurancePlanService insurancePlanService, PaymentService paymentService,
                          NotificationService notificationService) {
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.policyService = policyService;
        this.claimService = claimService;
        this.insurancePlanService = insurancePlanService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
    }
/**
 * Executes the index operation.
 */

    @GetMapping("/")
    public String index() {
        return "index";
    }
/**
 * Executes the login operation.
 */

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
/**
 * Executes the registerForm operation.
 */

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }
/**
 * Executes the registerUser operation.
 */

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user,
                               @RequestParam(value = "profilePhotoUpload", required = false) MultipartFile profilePhoto,
                               Model model) {
        try {
            userService.registerNewUser(user, profilePhoto);
            return "redirect:/login?success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }
/**
 * Executes the profilePhoto operation.
 */

    @GetMapping("/profile/photo")
    public ResponseEntity<byte[]> profilePhoto(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.notFound().build();
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null || user.getProfilePhoto() == null || user.getProfilePhotoContentType() == null) {
            return ResponseEntity.notFound().build();
        }
        MediaType mediaType = MediaType.parseMediaType(user.getProfilePhotoContentType());
        return ResponseEntity.ok().contentType(mediaType).body(user.getProfilePhoto());
    }
/**
 * Executes the dashboard operation.
 */

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(defaultValue = "overview") String tab,
                            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) {
            return "redirect:/admin/dashboard";
        }

        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null) {
            return "redirect:/login?error=true";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("activeTab", switch (tab) {
            case "vehicles", "policies", "claims", "payments", "notifications", "profile" -> tab;
            default -> "overview";
        });

        // Show every vehicle the customer owns (not just the latest) so
            // registering a new vehicle never makes an older one appear to
            // "disappear" — it was only ever a display limitation, nothing
            // was actually being deleted.
            List<Vehicle> vehicles = vehicleService.getVehiclesForUser(user);
            model.addAttribute("vehicles", vehicles);
            if (!vehicles.isEmpty()) {
                Vehicle latestVehicle = vehicles.get(vehicles.size() - 1);
                Policy currentPolicy = policyService.getCurrentPolicyForVehicle(latestVehicle);
                model.addAttribute("currentVehicle", latestVehicle);
                model.addAttribute("currentPolicy", currentPolicy);
            }
        // Every policy across every vehicle, for the Policies tab.
        List<Policy> policies = policyService.getPoliciesForUser(user);
        model.addAttribute("policies", policies);
        Set<Long> paidPolicyIds = new HashSet<>();
        for (Policy policy : policies) {
            if (paymentService.hasCompletedPaymentForPolicy(policy)) {
                paidPolicyIds.add(policy.getId());
            }
        }
        model.addAttribute("paidPolicyIds", paidPolicyIds);

        // Real claim + payment history for the sidebar tabs.
        model.addAttribute("claims", claimService.getClaimsForUser(user));
        List<Payment> payments = paymentService.getPaymentsForUser(user);
        model.addAttribute("payments", payments);
        model.addAttribute("totalPayments", payments.stream()
                .filter(payment -> payment.getStatus() == com.autoshield.enums.PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum());

        // Unread notification count for the header bell icon.
        model.addAttribute("unreadNotifications", notificationService.getUnreadCount(user));
        model.addAttribute("notifications", notificationService.getForUser(user));
        return "customer/dashboard";
    }
/**
 * Executes the paymentsPage operation.
 */

    @GetMapping("/payments")
    public String paymentsPage() {
        return "redirect:/dashboard?tab=payments";
    }
/**
 * Executes the profilePage operation.
 */

    @GetMapping("/profile")
    public String profilePage() {
        return "redirect:/dashboard?tab=profile";
    }
/**
 * Executes the forgotPassword operation.
 */

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }
/**
 * Executes the requestPasswordReset operation.
 */

    @PostMapping("/forgot-password")
    public String requestPasswordReset(@RequestParam("email") String email, Model model) {
        String token = userService.initiatePasswordReset(email);
        // NOTE: this demo has no email server configured, so the reset link is
        // shown directly on the page instead of being emailed. In a real
        // deployment, replace this with an actual email send and don't return
        // the link/token to the browser.
        if (token != null) {
            model.addAttribute("resetLink", "/reset-password?token=" + token);
        }
        model.addAttribute("submitted", true);
        return "auth/forgot-password";
    }
/**
 * Executes the resetPasswordForm operation.
 */

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "auth/reset-password";
    }
/**
 * Executes the resetPassword operation.
 */

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("token") String token,
                                @RequestParam("newPassword") String newPassword,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "New password and confirmation don't match.");
            return "auth/reset-password";
        }
        try {
            userService.resetPassword(token, newPassword);
            return "redirect:/login?resetSuccess=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }
/**
 * Executes the packages operation.
 */

    @GetMapping("/packages")
    public String packages(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            boolean isAdmin = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", user);
        } else {
            User mockUser = new User();
            mockUser.setFullName("Guest");
            mockUser.setRole("ROLE_GUEST");
            model.addAttribute("currentUser", mockUser);
        }
        model.addAttribute("packagesList", insurancePlanService.getAllPlans());
        return "policy/packages";
    }
/**
 * Executes the selectPackage operation.
 */

    @PostMapping("/packages/select")
    public String selectPackage(@RequestParam("selectedPlan") String selectedPlan) {
        return "redirect:/vehicle-details?selectedPlan=" + selectedPlan;
    }
/**
 * Executes the vehicleDetailsForm operation.
 */

    @GetMapping("/vehicle-details")
    public String vehicleDetailsForm(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam("selectedPlan") String selectedPlan,
                                     Model model) {
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", user);
        } else {
            User mockUser = new User();
            mockUser.setFullName("Guest");
            model.addAttribute("currentUser", mockUser);
        }
        model.addAttribute("selectedPlan", selectedPlan);
        model.addAttribute("currentYear", java.time.Year.now().getValue());
        return "policy/vehicle-details";
    }
/**
 * Processes the saveVehicleDetails operation.
 */

    @PostMapping("/vehicle-details")
    public String saveVehicleDetails(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam("selectedPlan") String selectedPlan,
                                     @RequestParam("vehicleCategory") String vehicleCategory,
                                     @RequestParam("vehicleColor") String vehicleColor,
                                     @RequestParam("vehicleYear") Integer vehicleYear,
                                     @RequestParam("fuelType") String fuelType,
                                     @RequestParam("chassisNumber") String chassisNumber,
                                     @RequestParam("vehicleNumber") String vehicleNumber,
                                     @RequestParam("vehicleModel") String vehicleModel,
                                     @RequestParam("vehicleImages") String vehicleImages,
                                     Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        try {
            Vehicle vehicle = vehicleService.registerVehicle(
                    user, vehicleNumber, vehicleModel, vehicleYear, fuelType, vehicleCategory, vehicleColor,
                    chassisNumber, vehicleImages);
            Policy policy = policyService.createOrRenewPolicy(vehicle, selectedPlan);
            return "redirect:/activation-success?policyId=" + policy.getId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("selectedPlan", selectedPlan);
            model.addAttribute("currentUser", user);
            model.addAttribute("currentYear", java.time.Year.now().getValue());
            return "policy/vehicle-details";
        }
    }
/**
 * Executes the activationSuccess operation.
 */

    @GetMapping("/activation-success")
    public String activationSuccess(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam("policyId") Long policyId,
                                    @RequestParam(defaultValue = "false") boolean paymentBlocked,
                                    Model model) {
        Policy policy = policyService.getById(policyId);
        if (policy == null || userDetails == null) {
            return "redirect:/dashboard";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null || !policy.getVehicle().getOwner().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("currentPolicy", policy);
        model.addAttribute("currentVehicle", policy.getVehicle());
        model.addAttribute("policyId", policyId);
        model.addAttribute("paymentBlocked", paymentBlocked);
        model.addAttribute("paymentCompleted", paymentService.hasCompletedPaymentForPolicy(policy));
        return "policy/activation-success";
    }

    // Epic 2, US-10: a distinct renewal action for an ACTIVE or EXPIRED policy
    // — unlike applying via /packages, this does not re-collect vehicle
    // details or go through officer approval again.
/**
 * Executes the renewPolicyForm operation.
 */
    @GetMapping("/policy/renew/{id}")
    public String renewPolicyForm(@PathVariable("id") Long id,
                                  @AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Policy policy = policyService.getById(id);
        if (policy == null) {
            return "redirect:/dashboard";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (!policy.getVehicle().getOwner().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        if (policy.getStatus() != com.autoshield.enums.PolicyStatus.APPROVED
                && policy.getStatus() != com.autoshield.enums.PolicyStatus.ACTIVE
                && policy.getStatus() != com.autoshield.enums.PolicyStatus.EXPIRED) {
            // Nothing to renew (e.g. still PENDING, or already REJECTED) — send
            // them to a fresh application instead.
            return "redirect:/packages";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("currentPolicy", policy);
        model.addAttribute("currentVehicle", policy.getVehicle());
        return "policy/renew";
    }
/**
 * Executes the renewPolicy operation.
 */

    @PostMapping("/policy/renew/{id}")
    public String renewPolicy(@PathVariable("id") Long id,
                              @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Policy oldPolicy = policyService.getById(id);
        if (oldPolicy == null) {
            return "redirect:/dashboard";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (!oldPolicy.getVehicle().getOwner().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        try {
            Policy newPolicy = policyService.renewPolicy(id, oldPolicy.getPlan().getPlanName());
            // Renewals skip approval and go straight to activation-success,
            // which will show the "Proceed to Payment" button since the new
            // policy is already ACTIVE.
            return "redirect:/activation-success?policyId=" + newPolicy.getId();
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard";
        }
    }
/**
 * Executes the showPaymentPortal operation.
 */

    @GetMapping("/payment")
    public String showPaymentPortal(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam("policyId") Long policyId,
                                    @RequestParam(value = "paymentError", required = false) String paymentError,
                                    Model model) {
        // Render the customer checkout only for an authenticated owner of an
        // approved policy that does not already have open coverage.
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", user);
        }
        Policy policy = policyService.getById(policyId);
        if (policy == null) {
            return "redirect:/dashboard";
        }
        // A policy can only be paid for once an officer/manager has approved
        // it (Epic 2, US-08). Until then, send the customer back to the
        // activation-success page, which shows the pending/rejected state.
        if (policy.getStatus() != com.autoshield.enums.PolicyStatus.APPROVED
                && policy.getStatus() != com.autoshield.enums.PolicyStatus.ACTIVE
                && policy.getStatus() != com.autoshield.enums.PolicyStatus.EXPIRED) {
            return "redirect:/activation-success?policyId=" + policyId;
        }
        if (paymentService.hasOpenPaymentForPolicy(policy) && !coverageHasExpired(policy)) {
            return "redirect:/activation-success?policyId=" + policyId + "&paymentBlocked=true";
        }
        model.addAttribute("currentPolicy", policy);
        model.addAttribute("currentVehicle", policy.getVehicle());
        model.addAttribute("policyId", policyId);
        model.addAttribute("paymentError", paymentError);
        return "payment/payment";
    }
/**
 * Executes the processPayment operation.
 */

    @PostMapping("/payment")
    public String processPayment(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("policyId") Long policyId,
                                 @RequestParam("paymentMethod") String paymentMethod,
                                 @RequestParam(value = "transactionReference", required = false) String transactionReference,
                                 @RequestParam(value = "paymentSlipData", required = false) String paymentSlipData) {
        // Revalidate policy access and payment eligibility on the server before
        // creating either an immediate card payment or a pending bank transfer.
        Policy policy = policyService.getById(policyId);
        if (policy == null) {
            return "redirect:/dashboard";
        }

        // Ownership check: a customer can only pay for their own policy.
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            if (!policy.getVehicle().getOwner().getId().equals(user.getId())) {
                return "redirect:/dashboard";
            }
        }

        // Re-check approval status server-side too, not just on the GET page.
        if (policy.getStatus() != com.autoshield.enums.PolicyStatus.APPROVED
                && policy.getStatus() != com.autoshield.enums.PolicyStatus.ACTIVE
                && policy.getStatus() != com.autoshield.enums.PolicyStatus.EXPIRED) {
            return "redirect:/activation-success?policyId=" + policyId;
        }
        if (paymentService.hasOpenPaymentForPolicy(policy) && !coverageHasExpired(policy)) {
            return "redirect:/activation-success?policyId=" + policyId + "&paymentBlocked=true";
        }

        Payment payment;
        if ("Bank Transfer".equals(paymentMethod)) {
            if (transactionReference == null
                    || !BANK_TRANSACTION_REFERENCE.matcher(transactionReference).matches()) {
                return "redirect:/payment?policyId=" + policyId
                        + "&paymentError=Bank+reference+must+contain+3+uppercase+letters+followed+by+11+numbers.";
            }
            payment = paymentService.submitBankTransfer(policy, transactionReference, paymentSlipData);
        } else {
            // Card payments are saved as completed payment records.
            payment = paymentService.processPayment(policy, paymentMethod);
        }

        return "redirect:/payment-success?paymentId=" + payment.getId();
    }

    private boolean coverageHasExpired(Policy policy) {
        return policy.getExpiryDate() != null && policy.getExpiryDate().isBefore(LocalDate.now());
    }
/**
 * Executes the showPaymentSuccess operation.
 */

    @GetMapping("/payment-success")
    public String showPaymentSuccess(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam("paymentId") Long paymentId,
                                     Model model) {
        // A receipt is private customer data, so verify ownership before
        // exposing the saved payment and its associated policy details.
        if (userDetails == null) {
            return "redirect:/login";
        }
        Payment payment = paymentService.getById(paymentId);
        if (payment == null) {
            return "redirect:/dashboard";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null || !payment.getPolicy().getVehicle().getOwner().getId().equals(user.getId())) {
            return "redirect:/dashboard";
        }
        model.addAttribute("currentUser", user);
        model.addAttribute("payment", payment);
        model.addAttribute("currentPolicy", payment.getPolicy());
        model.addAttribute("currentVehicle", payment.getPolicy().getVehicle());
        model.addAttribute("paymentMethod", payment.getPaymentMethod());
        model.addAttribute("receiptNumber", payment.getReceiptNumber());

        return "payment/payment-success";
    }
/**
 * Executes the adminDashboard operation.
 */

    @GetMapping("/admin/dashboard")
    public String adminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User admin = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", admin);
        }
        List<User> allUsers = userService.getAllUsers();
        model.addAttribute("users", allUsers);
        model.addAttribute("totalVehicles", allUsers.stream()
                .mapToLong(user -> user.getVehicles() == null ? 0 : user.getVehicles().size())
                .sum());
        model.addAttribute("totalRevenue", paymentService.getTotalRevenue());
        model.addAttribute("totalClaims", claimService.getAllClaims().size());
        return "admin/dashboard";
    }
/**
 * Processes the deleteUser operation.
 */

    @PostMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userService.findByEmail(userDetails.getUsername());
        if (admin != null && !admin.getId().equals(id)) {
            userService.deleteUser(id);
        }
        return "redirect:/admin/dashboard?deleted=true";
    }
/**
 * Executes the toggleUserStatus operation.
 */

    @PostMapping("/admin/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User admin = userService.findByEmail(userDetails.getUsername());
        // An admin can't deactivate their own account and lock themselves out.
        if (admin != null && !admin.getId().equals(id)) {
            userService.toggleActiveStatus(id);
        }
        return "redirect:/admin/dashboard?statusChanged=true";
    }
/**
 * Executes the adminRegisterForm operation.
 */

    @GetMapping("/admin/users/register")
    public String adminRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/register";
    }
/**
 * Executes the adminRegisterUser operation.
 */

    @PostMapping("/admin/users/register")
    public String adminRegisterUser(@ModelAttribute("user") User user,
                                    @RequestParam("role") String role,
                                    Model model) {
        try {
            userService.registerNewUserWithRole(user, role);
            return "redirect:/admin/dashboard?success=true";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/register";
        }
    }
}