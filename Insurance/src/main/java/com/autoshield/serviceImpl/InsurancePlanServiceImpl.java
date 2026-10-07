package com.autoshield.serviceImpl;

import com.autoshield.model.InsurancePlan;
import com.autoshield.repository.InsurancePlanRepository;
import com.autoshield.repository.PolicyRepository;
import com.autoshield.service.InsurancePlanService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Provides the InsurancePlanServiceImpl functionality.
 */

@Service
public class InsurancePlanServiceImpl implements InsurancePlanService {
/** The insurancePlanRepository value. */

    private final InsurancePlanRepository insurancePlanRepository;
/** The policyRepository value. */
    private final PolicyRepository policyRepository;
/**
 * Executes the InsurancePlanServiceImpl operation.
 */

    public InsurancePlanServiceImpl(InsurancePlanRepository insurancePlanRepository,
                                    PolicyRepository policyRepository) {
        this.insurancePlanRepository = insurancePlanRepository;
        this.policyRepository = policyRepository;
    }

    @Override
    public List<InsurancePlan> getAllPlans() {
        return insurancePlanRepository.findAll();
    }

    @Override
    public InsurancePlan getByName(String planName) {
        return insurancePlanRepository.findByPlanName(planName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown insurance plan: " + planName));
    }

    @Override
    public InsurancePlan getById(Long id) {
        return insurancePlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Insurance plan not found: " + id));
    }
/**
 * Processes the createPlan operation.
 */

    @Override
    public InsurancePlan createPlan(String planName, String coverage, Double basePrice, boolean featured) {
        InsurancePlan plan = new InsurancePlan(planName, coverage, basePrice);
        plan.setFeatured(featured);
        return insurancePlanRepository.save(plan);
    }
/**
 * Processes the updatePlan operation.
 */

    @Override
    public InsurancePlan updatePlan(Long id, String planName, String coverage, Double basePrice, boolean featured) {
        InsurancePlan plan = getById(id);
        plan.setPlanName(planName);
        plan.setCoverage(coverage);
        plan.setBasePrice(basePrice);
        plan.setFeatured(featured);
        return insurancePlanRepository.save(plan);
    }
/**
 * Processes the deletePlan operation.
 */

    @Override
    public void deletePlan(Long id) {
        InsurancePlan plan = getById(id);
        long policiesUsingPlan = policyRepository.countByPlan(plan);
        if (policiesUsingPlan > 0) {
            throw new IllegalStateException(
                    "Can't delete \"" + plan.getPlanName() + "\" — " + policiesUsingPlan +
                            " existing polic" + (policiesUsingPlan == 1 ? "y" : "ies") + " still use it.");
        }
        insurancePlanRepository.deleteById(id);
    }
}