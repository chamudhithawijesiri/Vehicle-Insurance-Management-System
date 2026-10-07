package com.autoshield;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoShieldApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoShieldApplication.class, args);
    }
}