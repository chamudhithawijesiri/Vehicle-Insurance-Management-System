package com.autoshield.service;

import java.util.List;

import com.autoshield.enums.PaymentStatus;
import com.autoshield.model.Payment;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
/**
 * Provides the PaymentService functionality.
 */

public interface PaymentService {
    // Records a completed premium payment against a policy and returns the saved record.
/**
     * Executes the processPayment operation.
     */
    Payment processPayment(Policy policy, String paymentMethod);

    // Bank Transfer: the customer submits proof of transfer (a slip/receipt
    // image or PDF, plus their bank's transaction reference) instead of an
    // instant card charge. Saved as PENDING until an admin/finance officer
    // verifies it against the bank statement and marks it COMPLETED or FAILED.
/**
     * Executes the submitBankTransfer operation.
     */
    Payment submitBankTransfer(Policy policy, String transactionReference, String slipFileData);

/**
     * Executes the getPaymentsForUser operation.
     */
    List<Payment> getPaymentsForUser(User user);

    // A failed or refunded payment does not prevent a customer from retrying.
/**
     * Executes the hasOpenPaymentForPolicy operation.
     */
    boolean hasOpenPaymentForPolicy(Policy policy);

    /**
     * Returns true only when a completed premium payment exists for the policy.
     */
    boolean hasCompletedPaymentForPolicy(Policy policy);

    // All payments across every customer (Epic 4, US-09 payment reports, and
    // the Finance Officer's reconciliation view, US-04).
/**
     * Executes the getAllPayments operation.
     */
    List<Payment> getAllPayments();

/**
     * Executes the getById operation.
     */
    Payment getById(Long id);

    // Total revenue across all completed payments (for the admin dashboard, Epic 4 US-07).
/**
     * Executes the getTotalRevenue operation.
     */
    double getTotalRevenue();

    // Epic 4, US-05: Finance Officer corrects a payment's status — e.g.
    // marking a bounced bank transfer FAILED, or processing a REFUNDED claim
    // payout, after reconciling it against the bank statement.
/**
     * Executes the updateStatus operation.
     */
    Payment updateStatus(Long paymentId, PaymentStatus newStatus);
}