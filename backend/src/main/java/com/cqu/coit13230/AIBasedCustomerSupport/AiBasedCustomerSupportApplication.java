package com.cqu.coit13230.AIBasedCustomerSupport;

import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Provides the main entry point for the AI-Based Customer Support
 * Automation System.
 *
 * <p>
 * This class initializes and starts the Spring Boot application,
 * enabling component scanning, auto-configuration, and application
 * configuration through the {@link SpringBootApplication} annotation.
 * </p>
 */
@SpringBootApplication

public class AiBasedCustomerSupportApplication {

    /**
     * Starts the Spring Boot customer support application.
     *
     * @param args command-line arguments supplied when starting the application
     */
    public static void main(String[] args) {

        SpringApplication.run(AiBasedCustomerSupportApplication.class, args);

    }

}