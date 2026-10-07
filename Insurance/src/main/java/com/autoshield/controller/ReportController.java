package com.autoshield.controller;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import com.autoshield.model.User;
import com.autoshield.service.ReportService;
import com.autoshield.service.UserService;
/**
 * Provides the ReportController functionality.
 */

@Controller
public class ReportController {
/** The reportService value. */

    private final ReportService reportService;
/** The userService value. */
    private final UserService userService;
/**
 * Executes the ReportController operation.
 */

    public ReportController(ReportService reportService, UserService userService) {
        this.reportService = reportService;
        this.userService = userService;
    }
/**
 * Executes the reportsPage operation.
 */

    @GetMapping("/admin/reports")
    public String reportsPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            User currentUser = userService.findByEmail(userDetails.getUsername());
            model.addAttribute("currentUser", currentUser);
        }
        return "admin/reports";
    }
/**
 * Executes the customerReport operation.
 */

    @GetMapping("/admin/reports/customers.csv")
    public ResponseEntity<byte[]> customerReport() {
        return csvResponse(reportService.generateCustomerReportCsv(), "customer-report");
    }
/**
 * Executes the policyReport operation.
 */

    @GetMapping("/admin/reports/policies.csv")
    public ResponseEntity<byte[]> policyReport() {
        return csvResponse(reportService.generatePolicyReportCsv(), "policy-report");
    }
/**
 * Executes the claimReport operation.
 */

    @GetMapping("/admin/reports/claims.csv")
    public ResponseEntity<byte[]> claimReport() {
        return csvResponse(reportService.generateClaimReportCsv(), "claims-report");
    }
/**
 * Executes the paymentReport operation.
 */

    @GetMapping("/admin/reports/payments.csv")
    public ResponseEntity<byte[]> paymentReport() {
        return csvResponse(reportService.generatePaymentReportCsv(), "payments-report");
    }

    private ResponseEntity<byte[]> csvResponse(String csvContent, String fileNamePrefix) {
        String fileName = fileNamePrefix + "-" + LocalDate.now() + ".csv";
        byte[] bytes = csvContent.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(bytes);
    }
}