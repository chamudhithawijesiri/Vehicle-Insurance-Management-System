package com.autoshield.service;

import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.model.Vehicle;

import java.util.List;
/**
 * Provides the PolicyService functionality.
 */

public interface PolicyService {
/**
     * Executes the createOrRenewPolicy operation.
     */
    Policy createOrRenewPolicy(Vehicle vehicle, String planName);

/**
     * Executes the renewPolicy operation.
     */
    Policy renewPolicy(Long existingPolicyId, String planName);

/**
     * Executes the getCurrentPolicyForVehicle operation.
     */
    Policy getCurrentPolicyForVehicle(Vehicle vehicle);

/**
     * Executes the getById operation.
     */
    Policy getById(Long id);

/**
     * Executes the getPoliciesForUser operation.
     */
    List<Policy> getPoliciesForUser(User user);

/**
     * Executes the getPendingPolicies operation.
     */
    List<Policy> getPendingPolicies();

/**
     * Executes the getAllPolicies operation.
     */
    List<Policy> getAllPolicies();

/**
     * Executes the approvePolicy operation.
     */
    Policy approvePolicy(Long policyId, String reviewNotes);

/**
     * Executes the rejectPolicy operation.
     */
    Policy rejectPolicy(Long policyId, String reviewNotes);

    /**
     * Activates an approved policy after its premium payment is completed.
     */
    Policy activateAfterPayment(Long policyId);

    // ---- Vehicle Surveyor workflow (Epic 2, US-06) ----

    // PENDING applications that haven't been physically inspected yet —
    // the Surveyor's queue.
/**
     * Executes the getPoliciesAwaitingInspection operation.
     */
    List<Policy> getPoliciesAwaitingInspection();

    // Records the surveyor's inspection findings on a policy application.
    // Does not itself approve/reject — that stays with the officer, who sees
    // this report as input to their decision.
/**
     * Executes the submitVehicleInspection operation.
     */
    Policy submitVehicleInspection(Long policyId, User surveyor, String report, boolean passed);

    // History view for the surveyor dashboard.
/**
     * Executes the getCompletedInspections operation.
     */
    List<Policy> getCompletedInspections();
}