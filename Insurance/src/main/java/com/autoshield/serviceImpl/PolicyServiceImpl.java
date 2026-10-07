package com.autoshield.serviceImpl;

import com.autoshield.enums.NotificationType;
import com.autoshield.enums.PolicyStatus;
import com.autoshield.model.InsurancePlan;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.model.Vehicle;
import com.autoshield.repository.PolicyRepository;
import com.autoshield.repository.UserRepository;
import com.autoshield.service.InsurancePlanService;
import com.autoshield.service.EmailService;
import com.autoshield.service.NotificationService;
import com.autoshield.service.PolicyService;
import com.autoshield.util.PolicyNumberGenerator;
import com.autoshield.util.PremiumCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * Provides the PolicyServiceImpl functionality.
 */

@Service
public class PolicyServiceImpl implements PolicyService {
/** The policyRepository value. */

    private final PolicyRepository policyRepository;
/** The insurancePlanService value. */
    private final InsurancePlanService insurancePlanService;
/** The premiumCalculator value. */
    private final PremiumCalculator premiumCalculator;
/** The policyNumberGenerator value. */
    private final PolicyNumberGenerator policyNumberGenerator;
/** The notificationService value. */
    private final NotificationService notificationService;
/** The emailService value. */
    private final EmailService emailService;
/** The userRepository value. */
    private final UserRepository userRepository;
/**
 * Executes the PolicyServiceImpl operation.
 */

    public PolicyServiceImpl(PolicyRepository policyRepository,
                             InsurancePlanService insurancePlanService,
                             PremiumCalculator premiumCalculator,
                             PolicyNumberGenerator policyNumberGenerator,
                             NotificationService notificationService,
                             EmailService emailService,
                             UserRepository userRepository) {
        this.policyRepository = policyRepository;
        this.insurancePlanService = insurancePlanService;
        this.premiumCalculator = premiumCalculator;
        this.policyNumberGenerator = policyNumberGenerator;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }
/**
 * Processes the createOrRenewPolicy operation.
 */

    @Override
    public Policy createOrRenewPolicy(Vehicle vehicle, String planName) {
        InsurancePlan plan = insurancePlanService.getByName(planName);
        double premium = premiumCalculator.calculate(vehicle.getVehicleCategory(), planName);

        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumberGenerator.generate());
        policy.setVehicle(vehicle);
        policy.setPlan(plan);
        policy.setPremium(premium);
        policy.setStatus(PolicyStatus.PENDING);

        Policy saved = policyRepository.save(policy);
        userRepository.findByRoleAndActiveTrue("ROLE_FINANCE").forEach(financeOfficer ->
                notificationService.notify(
                        financeOfficer,
                        NotificationType.POLICY_STATUS,
                        "New insurance application",
                        "Customer " + vehicle.getOwner().getFullName() + " submitted an application for "
                                + vehicle.getVehicleModel() + " (" + vehicle.getVehicleNumber()
                                + "). Please review it in the Finance workspace."
                )
        );
        return saved;
    }
/**
 * Executes the renewPolicy operation.
 */

    @Override
    public Policy renewPolicy(Long existingPolicyId, String planName) {
        Policy oldPolicy = policyRepository.findById(existingPolicyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + existingPolicyId));

        if (oldPolicy.getStatus() != PolicyStatus.ACTIVE && oldPolicy.getStatus() != PolicyStatus.EXPIRED) {
            throw new IllegalArgumentException("Only an active or expired policy can be renewed.");
        }

        Vehicle vehicle = oldPolicy.getVehicle();
        InsurancePlan plan = insurancePlanService.getByName(planName);
        double premium = premiumCalculator.calculate(vehicle.getVehicleCategory(), planName);

        Policy newPolicy = new Policy();
        newPolicy.setPolicyNumber(policyNumberGenerator.generate());
        newPolicy.setVehicle(vehicle);
        newPolicy.setPlan(plan);
        newPolicy.setPremium(premium);
        newPolicy.setStatus(PolicyStatus.APPROVED);
        Policy saved = policyRepository.save(newPolicy);

        oldPolicy.setStatus(PolicyStatus.RENEWED);
        policyRepository.save(oldPolicy);

        notificationService.notify(
                vehicle.getOwner(),
                NotificationType.POLICY_STATUS,
                "Policy renewed",
                "Your policy for " + vehicle.getVehicleModel() + " (" + vehicle.getVehicleNumber() + ") has been renewed as "
                        + newPolicy.getPolicyNumber() + ". Please complete payment to activate your coverage."
        );

        return saved;
    }

    @Override
    public Policy getCurrentPolicyForVehicle(Vehicle vehicle) {
        return policyRepository.findTopByVehicleOrderByIdDesc(vehicle).orElse(null);
    }

    @Override
    public Policy getById(Long id) {
        return policyRepository.findById(id).orElse(null);
    }

    @Override
    public List<Policy> getPoliciesForUser(User user) {
        return policyRepository.findByVehicle_Owner(user);
    }

    @Override
    public List<Policy> getPendingPolicies() {
        return policyRepository.findByStatusOrderByIdDesc(PolicyStatus.PENDING);
    }

    @Override
    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }
/**
 * Executes the approvePolicy operation.
 */

    @Override
    public Policy approvePolicy(Long policyId, String reviewNotes) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        if (policy.getStatus() != PolicyStatus.PENDING) {
            throw new IllegalArgumentException("Only pending applications can be approved.");
        }
        policy.setStatus(PolicyStatus.APPROVED);
        policy.setStartDate(null);
        policy.setExpiryDate(null);
        if (reviewNotes != null && !reviewNotes.isBlank()) {
            policy.setReviewNotes(reviewNotes);
        }
        Policy saved = policyRepository.save(policy);

        notificationService.notify(
                policy.getVehicle().getOwner(),
                NotificationType.POLICY_STATUS,
                "Policy approved",
                "Your policy " + policy.getPolicyNumber() + " has been approved. "
                        + "Complete the premium payment from your dashboard to activate coverage."
        );
        emailService.sendPolicyDecisionEmail(
                policy.getVehicle().getOwner().getEmail(),
                policy.getVehicle().getOwner().getFullName(),
                policy.getPolicyNumber(),
                true,
                reviewNotes);

        return saved;
    }

    @Override
    public Policy activateAfterPayment(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        if (policy.getStatus() != PolicyStatus.APPROVED
                && policy.getStatus() != PolicyStatus.ACTIVE
                && policy.getStatus() != PolicyStatus.EXPIRED) {
            throw new IllegalArgumentException("Only an approved policy can be activated after payment.");
        }
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setStartDate(LocalDate.now());
        policy.setExpiryDate(LocalDate.now().plusYears(1));
        return policyRepository.save(policy);
    }
/**
 * Executes the rejectPolicy operation.
 */

    @Override
    public Policy rejectPolicy(Long policyId, String reviewNotes) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        if (policy.getStatus() != PolicyStatus.PENDING) {
            throw new IllegalArgumentException("Only pending applications can be rejected.");
        }
        policy.setStatus(PolicyStatus.REJECTED);
        if (reviewNotes != null && !reviewNotes.isBlank()) {
            policy.setReviewNotes(reviewNotes);
        }
        Policy saved = policyRepository.save(policy);

        notificationService.notify(
                policy.getVehicle().getOwner(),
                NotificationType.POLICY_STATUS,
                "Policy application not approved",
                "Your policy application " + policy.getPolicyNumber() + " was not approved."
                        + (reviewNotes != null && !reviewNotes.isBlank() ? " Reason: " + reviewNotes : "")
        );
        emailService.sendPolicyDecisionEmail(
                policy.getVehicle().getOwner().getEmail(),
                policy.getVehicle().getOwner().getFullName(),
                policy.getPolicyNumber(),
                false,
                reviewNotes);

        return saved;
    }

    @Override
    public List<Policy> getPoliciesAwaitingInspection() {
        return policyRepository.findByStatusOrderByIdDesc(PolicyStatus.PENDING).stream()
                .filter(p -> p.getInspectionReport() == null || p.getInspectionReport().isBlank())
                .toList();
    }
/**
 * Processes the submitVehicleInspection operation.
 */

    @Override
    public Policy submitVehicleInspection(Long policyId, User surveyor, String report, boolean passed) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        policy.setInspectionReport(report);
        policy.setInspectionPassed(passed);
        policy.setInspectedBy(surveyor);
        policy.setInspectedAt(LocalDateTime.now());
        return policyRepository.save(policy);
    }

    @Override
    public List<Policy> getCompletedInspections() {
        return policyRepository.findByInspectionReportIsNotNullOrderByInspectedAtDesc();
    }
}