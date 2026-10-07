package com.autoshield.service;
/**
 * Provides the ReportService functionality.
 */

public interface ReportService {
    // Epic 4, US-08: policy and customer reports.
/**
     * Executes the generateCustomerReportCsv operation.
     */
    String generateCustomerReportCsv();
/**
     * Executes the generatePolicyReportCsv operation.
     */
    String generatePolicyReportCsv();

    // Epic 4, US-09: claims and payment reports.
/**
     * Executes the generateClaimReportCsv operation.
     */
    String generateClaimReportCsv();
/**
     * Executes the generatePaymentReportCsv operation.
     */
    String generatePaymentReportCsv();
}