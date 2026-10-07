package com.autoshield.serviceImpl;

import com.autoshield.model.Claim;
import com.autoshield.model.Payment;
import com.autoshield.model.Policy;
import com.autoshield.model.User;
import com.autoshield.service.ClaimService;
import com.autoshield.service.PaymentService;
import com.autoshield.service.PolicyService;
import com.autoshield.service.ReportService;
import com.autoshield.service.UserService;
import com.autoshield.util.CsvUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
/**
 * Provides the ReportServiceImpl functionality.
 */

@Service
public class ReportServiceImpl implements ReportService {
/** The userService value. */

    private final UserService userService;
/** The policyService value. */
    private final PolicyService policyService;
/** The claimService value. */
    private final ClaimService claimService;
/** The paymentService value. */
    private final PaymentService paymentService;
/**
 * Executes the ReportServiceImpl operation.
 */

    public ReportServiceImpl(UserService userService, PolicyService policyService,
                             ClaimService claimService, PaymentService paymentService) {
        this.userService = userService;
        this.policyService = policyService;
        this.claimService = claimService;
        this.paymentService = paymentService;
    }
/**
 * Processes the generateCustomerReportCsv operation.
 */

    @Override
    public String generateCustomerReportCsv() {
        List<String> headers = List.of("ID", "Full Name", "Email", "Phone Number", "Role", "Vehicles Registered");
        List<List<Object>> rows = new ArrayList<>();
        for (User user : userService.getAllUsers()) {
            rows.add(List.of(
                    user.getId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    user.getRole(),
                    user.getVehicles() != null ? user.getVehicles().size() : 0
            ));
        }
        return CsvUtil.buildCsv(headers, rows);
    }
/**
 * Processes the generatePolicyReportCsv operation.
 */

    @Override
    public String generatePolicyReportCsv() {
        List<String> headers = List.of("Policy Number", "Customer", "Vehicle Model", "Vehicle Number",
                "Plan", "Premium (Rs.)", "Status", "Start Date", "Expiry Date");
        List<List<Object>> rows = new ArrayList<>();
        for (Policy p : policyService.getAllPolicies()) {
            rows.add(List.of(
                    p.getPolicyNumber(),
                    p.getVehicle().getOwner().getFullName(),
                    p.getVehicle().getVehicleModel(),
                    p.getVehicle().getVehicleNumber(),
                    p.getPlan().getPlanName(),
                    p.getPremium(),
                    p.getStatus(),
                    p.getStartDate() != null ? p.getStartDate() : "",
                    p.getExpiryDate() != null ? p.getExpiryDate() : ""
            ));
        }
        return CsvUtil.buildCsv(headers, rows);
    }
/**
 * Processes the generateClaimReportCsv operation.
 */

    @Override
    public String generateClaimReportCsv() {
        List<String> headers = List.of("Claim Number", "Customer", "Policy Number", "Incident Date",
                "Amount Claimed (Rs.)", "Assessed Amount (Rs.)", "Status", "Submitted At");
        List<List<Object>> rows = new ArrayList<>();
        for (Claim c : claimService.getAllClaims()) {
            rows.add(List.of(
                    c.getClaimNumber(),
                    c.getPolicy().getVehicle().getOwner().getFullName(),
                    c.getPolicy().getPolicyNumber(),
                    c.getIncidentDate(),
                    c.getAmountClaimed() != null ? c.getAmountClaimed() : "",
                    c.getAssessedAmount() != null ? c.getAssessedAmount() : "",
                    c.getStatus(),
                    c.getSubmittedAt()
            ));
        }
        return CsvUtil.buildCsv(headers, rows);
    }
/**
 * Processes the generatePaymentReportCsv operation.
 */

    @Override
    public String generatePaymentReportCsv() {
        List<String> headers = List.of("Receipt Number", "Customer", "Policy Number", "Amount (Rs.)",
                "Method", "Status", "Paid At");
        List<List<Object>> rows = new ArrayList<>();
        for (Payment p : paymentService.getAllPayments()) {
            rows.add(List.of(
                    p.getReceiptNumber(),
                    p.getPolicy().getVehicle().getOwner().getFullName(),
                    p.getPolicy().getPolicyNumber(),
                    p.getAmount(),
                    p.getPaymentMethod(),
                    p.getStatus(),
                    p.getPaidAt()
            ));
        }
        return CsvUtil.buildCsv(headers, rows);
    }
}