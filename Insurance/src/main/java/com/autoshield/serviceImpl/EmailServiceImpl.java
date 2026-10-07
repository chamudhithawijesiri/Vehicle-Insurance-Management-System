package com.autoshield.serviceImpl;

import com.autoshield.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
/**
 * Provides the EmailServiceImpl functionality.
 */

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
/** The mailSender value. */

    private final JavaMailSender mailSender;
/** The fromAddress value. */
    private final String fromAddress;
/**
 * Executes the EmailServiceImpl operation.
 */

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${app.mail.from:${spring.mail.username:no-reply@autoshield.local}}")
                            String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }
/**
 * Processes the sendPolicyDecisionEmail operation.
 */

    @Override
    public void sendPolicyDecisionEmail(String recipient, String customerName, String policyNumber,
                                        boolean approved, String reviewNotes) {
        // Build the Finance Officer's approval/rejection message from the
        // decision and optional review notes.
        String decision = approved ? "approved" : "rejected";
        String subject = "Auto Shield policy application " + decision;
        StringBuilder body = new StringBuilder()
                .append("Dear ").append(customerName).append(",\n\n")
                .append("Your Auto Shield policy application ")
                .append(policyNumber).append(" has been ").append(decision).append(" by the Finance Officer.\n\n");

        if (approved) {
            body.append("You can now log in to your Auto Shield account and proceed with premium payment.\n");
        } else {
            body.append("The application will not proceed at this time.\n");
        }
        if (reviewNotes != null && !reviewNotes.isBlank()) {
            body.append("\nReview notes: ").append(reviewNotes.trim()).append('\n');
        }
        body.append("\nRegards,\nAuto Shield");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body.toString());

        try {
            // SMTP failures are logged so payment/policy persistence is not
            // undone solely because an email provider is temporarily unavailable.
            mailSender.send(message);
            logger.info("Sent policy {} email for {} to {}", decision, policyNumber, recipient);
        } catch (RuntimeException exception) {
            logger.error("Could not send policy {} email for {} to {}. "
                    + "Check spring.mail.* SMTP configuration.", decision, policyNumber, recipient, exception);
        }
    }
/**
 * Processes the sendPaymentStatusEmail operation.
 */

    @Override
    public void sendPaymentStatusEmail(String recipient, String customerName, String receiptNumber,
                                       String policyNumber, Double amount, String paymentMethod,
                                       String status) {
        // Send one customer-facing message for every Finance status update.
        String statusLabel = status.toLowerCase();
        String subject = "Auto Shield payment status updated: " + statusLabel;
        StringBuilder body = new StringBuilder()
                .append("Dear ").append(customerName).append(",\n\n")
                .append("The status of your Auto Shield payment has been updated by the Finance Officer.\n\n")
                .append("Payment details:\n")
                .append("- Receipt number: ").append(receiptNumber).append('\n')
                .append("- Policy number: ").append(policyNumber).append('\n')
                .append("- Amount: Rs. ").append(String.format("%,.2f", amount)).append('\n')
                .append("- Payment method: ").append(paymentMethod).append('\n')
                .append("- New status: ").append(statusLabel).append("\n\n");

        switch (status) {
            // Tailor the next-step guidance to the saved payment state.
            case "COMPLETED" -> body.append(
                    "Your payment has been confirmed. Thank you for your payment.\n");
            case "PENDING" -> body.append(
                    "Your payment is pending verification. We will notify you when the review is complete.\n");
            case "FAILED" -> body.append(
                    "Your payment could not be verified. Please log in to your Auto Shield account to make the payment again.\n");
            case "REFUNDED" -> body.append(
                    "Your payment has been refunded. Please log in to your Auto Shield account for more details.\n");
            default -> body.append(
                    "Please log in to your Auto Shield account for more details.\n");
        }
        body.append("\nRegards,\nAuto Shield");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body.toString());

        try {
            // Delivery errors are recorded for operations review while the
            // already-saved payment status remains authoritative.
            mailSender.send(message);
            logger.info("Sent payment status email for receipt {} to {}", receiptNumber, recipient);
        } catch (RuntimeException exception) {
            logger.error("Could not send payment status email for receipt {} to {}. "
                    + "Check spring.mail.* SMTP configuration.", receiptNumber, recipient, exception);
        }
    }
}
