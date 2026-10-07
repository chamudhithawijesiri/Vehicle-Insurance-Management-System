package com.autoshield.util;

import org.springframework.stereotype.Component;

/**
 * Calculates the premium for a vehicle category + insurance plan combination.
 * Extracted from the original UserServiceImpl so it can be reused by
 * VehicleService/PolicyService without depending on User at all.
 */
/**
 * Provides the PremiumCalculator functionality.
 */
@Component
public class PremiumCalculator {
/**
 * Processes the calculate operation.
 */

    public double calculate(String category, String plan) {
        String cleanCategory = normalizeCategory(category);
        String cleanPlan = normalizePlan(plan);

        return switch (cleanCategory) {
            case "Sedan" -> switch (cleanPlan) {
                case "Third Party" -> 3000.0;
                case "Comprehensive" -> 85000.0;
                case "Full Insurance" -> 110000.0;
                case "Commercial" -> 125000.0;
                default -> 85000.0;
            };
            case "SUV" -> switch (cleanPlan) {
                case "Third Party" -> 3500.0;
                case "Comprehensive" -> 125000.0;
                case "Full Insurance" -> 155000.0;
                case "Commercial" -> 175000.0;
                default -> 125000.0;
            };
            case "Coupe" -> switch (cleanPlan) {
                case "Third Party" -> 3000.0;
                case "Comprehensive" -> 140000.0;
                case "Full Insurance" -> 175000.0;
                case "Commercial" -> 195000.0;
                default -> 140000.0;
            };
            case "Hatchback" -> switch (cleanPlan) {
                case "Third Party" -> 2500.0;
                case "Comprehensive" -> 65000.0;
                case "Full Insurance" -> 85000.0;
                case "Commercial" -> 100000.0;
                default -> 65000.0;
            };
            case "Commercial Truck" -> switch (cleanPlan) {
                case "Third Party" -> 6000.0;
                case "Comprehensive" -> 150000.0;
                case "Full Insurance" -> 185000.0;
                case "Commercial" -> 210000.0;
                default -> 150000.0;
            };
            case "Motorcycle" -> switch (cleanPlan) {
                case "Third Party" -> 1000.0;
                case "Comprehensive" -> 12000.0;
                case "Full Insurance" -> 16000.0;
                case "Commercial" -> 20000.0;
                default -> 12000.0;
            };
            case "Van" -> switch (cleanPlan) {
                case "Third Party" -> 5000.0;
                case "Comprehensive" -> 120000.0;
                case "Full Insurance" -> 150000.0;
                case "Commercial" -> 175000.0;
                default -> 120000.0;
            };
            case "Convertible" -> switch (cleanPlan) {
                case "Third Party" -> 3000.0;
                case "Comprehensive" -> 145000.0;
                case "Full Insurance" -> 180000.0;
                case "Commercial" -> 200000.0;
                default -> 145000.0;
            };
            case "Station Wagon" -> switch (cleanPlan) {
                case "Third Party" -> 3000.0;
                case "Comprehensive" -> 75000.0;
                case "Full Insurance" -> 95000.0;
                case "Commercial" -> 110000.0;
                default -> 75000.0;
            };
            case "Pickup Truck" -> switch (cleanPlan) {
                case "Third Party" -> 6000.0;
                case "Comprehensive" -> 160000.0;
                case "Full Insurance" -> 195000.0;
                case "Commercial" -> 220000.0;
                default -> 160000.0;
            };
            case "Bus" -> switch (cleanPlan) {
                case "Third Party" -> 8000.0;
                case "Comprehensive" -> 200000.0;
                case "Full Insurance" -> 250000.0;
                case "Commercial" -> 280000.0;
                default -> 200000.0;
            };
            case "Three-Wheeler" -> switch (cleanPlan) {
                case "Third Party" -> 800.0;
                case "Comprehensive" -> 8000.0;
                case "Full Insurance" -> 12000.0;
                case "Commercial" -> 15000.0;
                default -> 8000.0;
            };
            default -> 85000.0;
        };
    }

    private String normalizePlan(String plan) {
        if (plan == null) return "Comprehensive";
        return switch (plan) {
            case "Motor Third Party" -> "Third Party";
            case "Motor Comprehensive" -> "Comprehensive";
            case "Motor Full Insurance" -> "Full Insurance";
            case "Commercial Vehicle Insurance" -> "Commercial";
            default -> "Comprehensive";
        };
    }

    private String normalizeCategory(String category) {
        if (category == null) return "Sedan";
        return switch (category) {
            case "Sedan" -> "Sedan";
            case "SUV" -> "SUV";
            case "Coupe" -> "Coupe";
            case "Hatchback" -> "Hatchback";
            case "Truck", "Commercial Truck" -> "Commercial Truck";
            case "Motorcycle" -> "Motorcycle";
            case "Van", "Van / Caravan" -> "Van";
            case "Convertible" -> "Convertible";
            case "Station Wagon" -> "Station Wagon";
            case "Pickup Truck" -> "Pickup Truck";
            case "Bus" -> "Bus";
            case "Three-Wheeler" -> "Three-Wheeler";
            default -> "Sedan";
        };
    }
}