package com.autoshield.util;

import org.springframework.stereotype.Component;
import java.util.Random;
/**
 * Provides the ClaimNumberGenerator functionality.
 */

@Component
public class ClaimNumberGenerator {
/** The random value. */

    private final Random random = new Random();
/**
 * Processes the generate operation.
 */

    public String generate() {
        int number = 1000000 + random.nextInt(9000000);
        return "CLM-" + number + "-AS";
    }
}