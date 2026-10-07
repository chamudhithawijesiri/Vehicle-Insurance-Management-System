package com.autoshield.util;

import org.springframework.stereotype.Component;
import java.util.Random;
/**
 * Provides the ReceiptNumberGenerator functionality.
 */

@Component
public class ReceiptNumberGenerator {
/** The random value. */

    private final Random random = new Random();
/**
 * Processes the generate operation.
 */

    public String generate() {
        int number = 100000 + random.nextInt(900000);
        return "AS-RCP-2026-" + number;
    }
}