package com.autoshield.util;

import org.springframework.stereotype.Component;
import java.util.Random;
/**
 * Provides the PolicyNumberGenerator functionality.
 */

@Component
public class PolicyNumberGenerator {
/** The random value. */

    private final Random random = new Random();
/**
 * Processes the generate operation.
 */

    public String generate() {
        int number = 1000000 + random.nextInt(9000000);
        return "AS-" + number + "-GP";
    }
}