package com.autoshield.model;

import com.autoshield.enums.PaymentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
/**
 * Provides the Payment functionality.
 */

@Entity
@Table(name = "payments")
public class Payment {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The receiptNumber value. */

    @Column(name = "receipt_number", nullable = false, unique = true)
    private String receiptNumber;

    // The policy this premium payment is for. From the policy we can reach
    // both the vehicle and the customer.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;
/** The amount value. */

    @Column(nullable = false)
    private Double amount;
/** The paymentMethod value. */

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod; // "Credit Card", "Bank Transfer", "Cash"
/** The status value. */

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.COMPLETED;
/** The paidAt value. */

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt = LocalDateTime.now();

    // Bank Transfer specific (US: customer uploads proof of transfer instead
    // of instant card processing). Both null for Credit Card / Debit Card /
    // Cash payments.
    @Column(name = "transaction_reference")
    private String transactionReference;
/** The paymentSlipData value. */

    @Lob
    @Column(name = "payment_slip_data", columnDefinition = "LONGTEXT")
    private String paymentSlipData;
/**
 * Executes the Payment operation.
 */

    public Payment() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getPaymentSlipData() {
        return paymentSlipData;
    }

    public void setPaymentSlipData(String paymentSlipData) {
        this.paymentSlipData = paymentSlipData;
    }
}