package com.autoshield.repository;

import com.autoshield.enums.PolicyStatus;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Provides the PolicyRepository functionality.
 */

public interface PolicyRepository extends JpaRepository<Policy, Long> {
/**
     * Executes the findByVehicle operation.
     */
    List<Policy> findByVehicle(Vehicle vehicle);
/**
     * Executes the findByPolicyNumber operation.
     */
    Optional<Policy> findByPolicyNumber(String policyNumber);

    // Most recent policy for a vehicle (used to show "current" policy on dashboard)
/**
     * Executes the findTopByVehicleOrderByIdDesc operation.
     */
    Optional<Policy> findTopByVehicleOrderByIdDesc(Vehicle vehicle);

    // Every policy belonging to a customer, across all their vehicles.
    // Used by the claim submission form so the customer can pick which
    // policy the claim is against.
/**
     * Executes the findByVehicle_Owner operation.
     */
    List<Policy> findByVehicle_Owner(User owner);

    // Officer/Manager review queue (Epic 2, US-05 through US-08).
/**
     * Executes the findByStatusOrderByIdDesc operation.
     */
    List<Policy> findByStatusOrderByIdDesc(PolicyStatus status);

    // Guards against deleting an InsurancePlan that policies still reference.
/**
     * Executes the countByPlan operation.
     */
    long countByPlan(com.autoshield.model.InsurancePlan plan);

    // History view: every policy a surveyor HAS already inspected, newest first.
/**
     * Executes the findByInspectionReportIsNotNullOrderByInspectedAtDesc operation.
     */
    List<Policy> findByInspectionReportIsNotNullOrderByInspectedAtDesc();
}