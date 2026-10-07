package com.autoshield.service;

import com.autoshield.model.InsurancePlan;

import java.util.List;
/**
 * Provides the InsurancePlanService functionality.
 */

public interface InsurancePlanService {
/**
     * Executes the getAllPlans operation.
     */
    List<InsurancePlan> getAllPlans();
/**
     * Executes the getByName operation.
     */
    InsurancePlan getByName(String planName);

    // Admin package management — lets the Admin create/edit/remove the
    // packages customers see on the /packages selection page, instead of
    // these being hardcoded.
/**
     * Executes the getById operation.
     */
    InsurancePlan getById(Long id);

/**
     * Executes the createPlan operation.
     */
    InsurancePlan createPlan(String planName, String coverage, Double basePrice, boolean featured);

/**
     * Executes the updatePlan operation.
     */
    InsurancePlan updatePlan(Long id, String planName, String coverage, Double basePrice, boolean featured);

/**
     * Executes the deletePlan operation.
     */
    void deletePlan(Long id);
}