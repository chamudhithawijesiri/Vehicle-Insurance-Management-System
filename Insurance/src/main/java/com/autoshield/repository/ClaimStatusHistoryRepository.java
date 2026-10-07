package com.autoshield.repository;

import com.autoshield.model.Claim;
import com.autoshield.model.ClaimStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Provides the ClaimStatusHistoryRepository functionality.
 */

public interface ClaimStatusHistoryRepository extends JpaRepository<ClaimStatusHistory, Long> {
    // Newest decision first (Problem 1: lets the admin see the full trail).
/**
     * Executes the findByClaimOrderByChangedAtDesc operation.
     */
    List<ClaimStatusHistory> findByClaimOrderByChangedAtDesc(Claim claim);
}