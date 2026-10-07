package com.autoshield.util;

import org.springframework.stereotype.Component;
import java.util.Random;
/**
 * Provides the TicketNumberGenerator functionality.
 */

@Component
public class TicketNumberGenerator {
/** The random value. */

    private final Random random = new Random();

    // Epic 5, US-02: a unique, human-readable ticket number issued on submission.
/**
 * Processes the generate operation.
 */
    public String generate() {
        int number = 1000000 + random.nextInt(9000000);
        return "TKT-" + number + "-AS";
    }
}