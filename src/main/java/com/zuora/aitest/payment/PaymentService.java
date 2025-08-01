package com.zuora.aitest.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main service class for handling payment operations.
 */
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    /**
     * Process a payment transaction.
     *
     * @param paymentRequest the payment request details
     * @return payment response with transaction status
     */
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        logger.info("Processing payment for amount: {}", paymentRequest.getAmount());

        try {
            // TODO: Implement actual payment processing logic
            // This could involve calling external payment APIs

            // For now, simulate successful payment
            PaymentResponse response = new PaymentResponse();
            response.setTransactionId(generateTransactionId());
            response.setStatus("SUCCESS");
            response.setMessage("Payment processed successfully");

            logger.info("Payment processed successfully with transaction ID: {}",
                       response.getTransactionId());

            return response;

        } catch (Exception e) {
            logger.error("Error processing payment", e);

            PaymentResponse response = new PaymentResponse();
            response.setStatus("FAILED");
            response.setMessage("Payment processing failed: " + e.getMessage());

            return response;
        }
    }

    private String generateTransactionId() {
        return "TXN_" + System.currentTimeMillis();
    }
}
