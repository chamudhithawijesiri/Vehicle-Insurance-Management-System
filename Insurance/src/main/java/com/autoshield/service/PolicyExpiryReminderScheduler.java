package com.autoshield.service;

import com.autoshield.enums.NotificationType;
import com.autoshield.enums.PolicyStatus;
import com.autoshield.model.Policy;
import com.autoshield.repository.NotificationRepository;
import com.autoshield.repository.PolicyRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Epic 4, US-10 (policy expiry reminders) and Epic 2, US-10 (renewal
 * eligibility). Runs once a day: (1) notifies any customer whose active
 * policy expires within the next 30 days, and (2) flips any ACTIVE policy
 * whose expiry date has already passed to EXPIRED, since renewPolicy() only
 * accepts ACTIVE or EXPIRED policies as eligible for renewal.
 */
/**
 * Provides the PolicyExpiryReminderScheduler functionality.
 */
@Component
public class PolicyExpiryReminderScheduler {
/** The policyRepository value. */

    private final PolicyRepository policyRepository;
/** The notificationRepository value. */
    private final NotificationRepository notificationRepository;
/** The notificationService value. */
    private final NotificationService notificationService;
/**
 * Executes the PolicyExpiryReminderScheduler operation.
 */

    public PolicyExpiryReminderScheduler(PolicyRepository policyRepository,
                                         NotificationRepository notificationRepository,
                                         NotificationService notificationService) {
        this.policyRepository = policyRepository;
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    // Runs once a day at 8 AM server time.
/**
 * Executes the runDailyPolicyChecks operation.
 */
    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyPolicyChecks() {
        LocalDate today = LocalDate.now();
        LocalDate cutoff = today.plusDays(30);

        for (Policy policy : policyRepository.findAll()) {
            if (policy.getStatus() != PolicyStatus.ACTIVE) continue;
            if (policy.getExpiryDate() == null) continue;

            // Past expiry date: mark it EXPIRED so it becomes eligible for renewal.
            if (policy.getExpiryDate().isBefore(today)) {
                policy.setStatus(PolicyStatus.EXPIRED);
                policyRepository.save(policy);
                continue;
            }

            // Within the reminder window: send a one-time "expiring soon" notice.
            if (policy.getExpiryDate().isAfter(cutoff)) continue;

            String reminderMarker = policy.getPolicyNumber() + " is expiring";
            boolean alreadySent = notificationRepository.existsByUserAndTitleContaining(
                    policy.getVehicle().getOwner(), reminderMarker);
            if (alreadySent) continue;

            notificationService.notify(
                    policy.getVehicle().getOwner(),
                    NotificationType.POLICY_EXPIRY,
                    reminderMarker,
                    "Your policy " + policy.getPolicyNumber() + " expires on " + policy.getExpiryDate()
                            + ". Renew it from your dashboard to stay covered."
            );
        }
    }
}