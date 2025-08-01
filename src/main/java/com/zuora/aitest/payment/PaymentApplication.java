package com.zuora.aitest.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

/**
 * Main application class for the Payment Integration system.
 */
public class PaymentApplication {

    private static final Logger logger = LoggerFactory.getLogger(PaymentApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Payment Integration Application");

        PaymentService paymentService = new PaymentService();

        // Create a sample payment request
        PaymentRequest request = new PaymentRequest(
            new BigDecimal("99.99"),
            "USD",
            "customer@example.com",
            "CREDIT_CARD",
            "Sample payment transaction"
        );

        logger.info("Processing sample payment: {}", request);

        // Process the payment
        PaymentResponse response = paymentService.processPayment(request);

        logger.info("Payment result: {}", response);

        System.out.println("Payment processed with status: " + response.getStatus());
        System.out.println("Transaction ID: " + response.getTransactionId());
        System.out.println("Message: " + response.getMessage());
    }
}
