package com.autoshield.repository;

import com.autoshield.model.Payment;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Provides the PaymentRepository functionality.
 */

public interface PaymentRepository extends JpaRepository<Payment, Long> {
/**
     * Executes the findByReceiptNumber operation.
     */
    Optional<Payment> findByReceiptNumber(String receiptNumber);
/**
     * Executes the findByPolicy operation.
     */
    List<Payment> findByPolicy(Policy policy);

    // Every payment made by a given customer, across all their policies.
/**
     * Executes the findByPolicy_Vehicle_OwnerOrderByPaidAtDesc operation.
     */
    List<Payment> findByPolicy_Vehicle_OwnerOrderByPaidAtDesc(User owner);
}