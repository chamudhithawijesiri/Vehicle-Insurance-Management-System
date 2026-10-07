package com.autoshield.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autoshield.enums.NotificationType;
import com.autoshield.enums.PaymentStatus;
import com.autoshield.model.Payment;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.repository.PaymentRepository;
import com.autoshield.repository.UserRepository;
import com.autoshield.service.EmailService;
import com.autoshield.service.NotificationService;
import com.autoshield.service.PaymentService;
import com.autoshield.service.PolicyService;
import com.autoshield.util.ReceiptNumberGenerator;
/**
 * Provides the PaymentServiceImpl functionality.
 */

@Service
public class PaymentServiceImpl implements PaymentService {
/** The paymentRepository value. */

    private final PaymentRepository paymentRepository;
/** The receiptNumberGenerator value. */
    private final ReceiptNumberGenerator receiptNumberGenerator;
/** The notificationService value. */
    private final NotificationService notificationService;
/** The emailService value. */
    private final EmailService emailService;
/** The userRepository value. */
    private final UserRepository userRepository;
    private final PolicyService policyService;
/**
 * Executes the PaymentServiceImpl operation.
 */

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              ReceiptNumberGenerator receiptNumberGenerator,
                              NotificationService notificationService,
                              EmailService emailService,
                              UserRepository userRepository,
                              PolicyService policyService) {
        this.paymentRepository = paymentRepository;
        this.receiptNumberGenerator = receiptNumberGenerator;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.policyService = policyService;
    }
/**
 * Executes the processPayment operation.
 */

    @Override
    public Payment processPayment(Policy policy, String paymentMethod) {
        // Card payments are treated as completed immediately and notify both
        // the customer and Finance Officer after the record is persisted.
        Payment payment = new Payment();
        payment.setReceiptNumber(receiptNumberGenerator.generate());
        payment.setPolicy(policy);
        payment.setAmount(policy.getPremium());
        payment.setPaymentMethod(paymentMethod);
        payment.setStatus(PaymentStatus.COMPLETED);
        Payment saved = paymentRepository.save(payment);
        policyService.activateAfterPayment(policy.getId());

        // Epic 4, US-10: notify the customer their payment went through.
        notificationService.notify(
                policy.getVehicle().getOwner(),
                NotificationType.PAYMENT,
                "Payment received",
                "Your payment of Rs. " + String.format("%,.2f", payment.getAmount())
                        + " for policy " + policy.getPolicyNumber() + " was successful. Receipt: " + saved.getReceiptNumber()
        );
        notifyFinanceOfficers(saved, "New payment received",
                "A customer submitted a " + saved.getPaymentMethod() + " payment of Rs. "
                        + String.format("%,.2f", saved.getAmount()) + " for policy "
                        + policy.getPolicyNumber() + ". Receipt: " + saved.getReceiptNumber() + ".");

        return saved;
    }
/**
 * Processes the submitBankTransfer operation.
 */

    @Override
    public Payment submitBankTransfer(Policy policy, String transactionReference, String slipFileData) {
        // Bank transfers remain pending until Finance verifies the reference
        // and uploaded slip against the bank statement.
        Payment payment = new Payment();
        payment.setReceiptNumber(receiptNumberGenerator.generate());
        payment.setPolicy(policy);
        payment.setAmount(policy.getPremium());
        payment.setPaymentMethod("Bank Transfer");
        // Unlike processPayment(), a bank transfer isn't verifiable
        // instantly — it stays PENDING until an admin checks the slip
        // against the actual bank statement (see AdminController/updateStatus).
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionReference(transactionReference);
        payment.setPaymentSlipData(slipFileData);
        Payment saved = paymentRepository.save(payment);

        notificationService.notify(
                policy.getVehicle().getOwner(),
                NotificationType.PAYMENT,
                "Bank transfer submitted",
                "We've received your bank transfer details for policy " + policy.getPolicyNumber()
                        + " (Rs. " + String.format("%,.2f", payment.getAmount()) + "). "
                        + "It's pending verification — we'll notify you once it's confirmed."
        );
        notifyFinanceOfficers(saved, "New payment requires verification",
                "A customer submitted a bank transfer of Rs. "
                        + String.format("%,.2f", saved.getAmount()) + " for policy "
                        + policy.getPolicyNumber() + ". Receipt: " + saved.getReceiptNumber()
                        + ". Please review it in Payment Records.");

        return saved;
    }

    private void notifyFinanceOfficers(Payment payment, String title, String message) {
        // Notify every active Finance Officer so the reconciliation queue stays
        // current without exposing payment records to inactive accounts.
        userRepository.findByRoleAndActiveTrue("ROLE_FINANCE")
                .forEach(financeOfficer -> notificationService.notify(
                        financeOfficer, NotificationType.PAYMENT, title, message));
    }

    @Override
    public List<Payment> getPaymentsForUser(User user) {
        // Return the customer's payments newest first for dashboard history.
        return paymentRepository.findByPolicy_Vehicle_OwnerOrderByPaidAtDesc(user);
    }
/**
 * Executes the hasOpenPaymentForPolicy operation.
 */

    @Override
    public boolean hasOpenPaymentForPolicy(Policy policy) {
        // Failed and refunded records do not block a customer from retrying;
        // pending and completed records keep the policy from duplicate payment.
        return paymentRepository.findByPolicy(policy).stream()
                .anyMatch(payment -> payment.getStatus() != PaymentStatus.FAILED
                        && payment.getStatus() != PaymentStatus.REFUNDED);
    }

    @Override
    public boolean hasCompletedPaymentForPolicy(Policy policy) {
        return paymentRepository.findByPolicy(policy).stream()
                .anyMatch(payment -> payment.getStatus() == PaymentStatus.COMPLETED);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Payment getById(Long id) {
        return paymentRepository.findById(id).orElse(null);
    }

    @Override
    public double getTotalRevenue() {
        // Revenue counts only completed payments, excluding pending, failed,
        // and refunded transactions from Finance reporting.
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
    }
/**
 * Processes the updateStatus operation.
 */

    @Override
    public Payment updateStatus(Long paymentId, PaymentStatus newStatus) {
        // Finance/Admin status changes are persisted first, then communicated
        // to the customer by email and in-app notifications.
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found: " + paymentId));
        payment.setStatus(newStatus);
        Payment saved = paymentRepository.save(payment);
        if (newStatus == PaymentStatus.COMPLETED) {
            policyService.activateAfterPayment(payment.getPolicy().getId());
        }
        User owner = payment.getPolicy().getVehicle().getOwner();
        emailService.sendPaymentStatusEmail(
                owner.getEmail(),
                owner.getFullName(),
                saved.getReceiptNumber(),
                payment.getPolicy().getPolicyNumber(),
                saved.getAmount(),
                saved.getPaymentMethod(),
                newStatus.name());

        // Only bother the customer for outcomes that actually affect them —
        // not routine re-confirmation of an already-completed payment.
        if (newStatus == PaymentStatus.FAILED || newStatus == PaymentStatus.REFUNDED) {
            String message = newStatus == PaymentStatus.FAILED
                    ? "Your payment " + payment.getReceiptNumber() + " could not be verified and has been marked as failed. Please make the payment again from your dashboard."
                    : "Your payment " + payment.getReceiptNumber() + " of Rs. " + String.format("%,.2f", payment.getAmount()) + " has been refunded.";
            notificationService.notify(owner, NotificationType.PAYMENT,
                    "Payment " + newStatus.name().toLowerCase(), message);
        } else if (newStatus == PaymentStatus.COMPLETED && "Bank Transfer".equals(payment.getPaymentMethod())) {
            // A bank transfer moving to COMPLETED means an admin just verified
            // it against the bank statement — worth telling the customer,
            // since (unlike card payments) they've been waiting on this.
            notificationService.notify(owner, NotificationType.PAYMENT,
                    "Bank transfer verified",
                    "Your bank transfer " + payment.getReceiptNumber() + " of Rs. "
                            + String.format("%,.2f", payment.getAmount()) + " has been verified and confirmed.");
        }

        return saved;
    }
}