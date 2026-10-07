package com.autoshield.service;
/**
 * Provides the EmailService functionality.
 */

public interface EmailService {
    void sendPolicyDecisionEmail(String recipient, String customerName, String policyNumber,
                                 boolean approved, String reviewNotes);

    void sendPaymentStatusEmail(String recipient, String customerName, String receiptNumber,
                                String policyNumber, Double amount, String paymentMethod,
                                String status);
}
