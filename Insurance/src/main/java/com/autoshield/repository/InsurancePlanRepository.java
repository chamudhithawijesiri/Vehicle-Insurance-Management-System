package com.autoshield.repository;

import com.autoshield.model.InsurancePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
/**
 * Provides the InsurancePlanRepository functionality.
 */

public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, Long> {
/**
     * Executes the findByPlanName operation.
     */
    Optional<InsurancePlan> findByPlanName(String planName);
}