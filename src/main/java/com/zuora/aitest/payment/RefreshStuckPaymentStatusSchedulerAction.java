package com.zuora.aitest.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class RefreshStuckPaymentStatusSchedulerAction {

    private static final Logger logger = LoggerFactory.getLogger(RefreshStuckPaymentStatusSchedulerAction.class);
    private static final int STUCK_PAYMENT_TIMEOUT_MINUTES = 15;

    public void processStuckPayments() {
        logger.info("Starting RefreshStuckPaymentStatusSchedulerAction");

        Instant cutoffTime = Instant.now().minus(STUCK_PAYMENT_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        List<Payment> stuckPayments = findStuckPayments(cutoffTime);
        logger.info("Found {} payments stuck in Processing", stuckPayments.size());

        for (Payment payment : stuckPayments) {
            try {
                processStuckPayment(payment);
            } catch (Exception e) {
                logger.error("Error processing stuck payment {}", payment.getId(), e);
            }
        }

        logger.info("Completed RefreshStuckPaymentStatusSchedulerAction");
    }

    private void processStuckPayment(Payment payment) {
        TransactionLog log = findTransactionLog(payment.getId());

        if (log == null || log.isBlank()) {
            logger.error("Payment {} has no valid transaction log after {} minutes, transitioning to ERROR",
                payment.getId(), STUCK_PAYMENT_TIMEOUT_MINUTES);

            payment.setStatus(PaymentStatus.ERROR);
            payment.setErrorMessage("TIMED_OUT_WAITING_FOR_TRANSACTION_LOG");

            logger.info("Payment {} transitioned to ERROR status", payment.getId());

        } else if ("PENDING".equals(log.getResponseCode())) {
            logger.warn("Payment {} still has PENDING transaction log after {} minutes",
                payment.getId(), STUCK_PAYMENT_TIMEOUT_MINUTES);

            checkGatewayStatusOrError(payment, log);

        } else {
            logger.info("Payment {} has valid transaction log, updating status", payment.getId());
            updatePaymentFromLog(payment, log);
        }
    }

    private void checkGatewayStatusOrError(Payment payment, TransactionLog log) {
        logger.info("Checking gateway status for payment {}", payment.getId());

        payment.setStatus(PaymentStatus.ERROR);
        payment.setErrorMessage("Gateway response timeout - no final status received");
        log.setResponseCode("TIMEOUT");
        log.setResponseMsg("No response received from gateway within timeout period");

        logger.warn("Payment {} marked as ERROR due to gateway timeout", payment.getId());
    }

    private void updatePaymentFromLog(Payment payment, TransactionLog log) {
        logger.info("Updating payment {} from transaction log {}", payment.getId(), log.getId());

        if ("SUCCESS".equals(log.getResponseCode()) || "APPROVED".equals(log.getResponseCode())) {
            payment.setStatus(PaymentStatus.PROCESSED);
            logger.info("Payment {} marked as PROCESSED", payment.getId());
        } else {
            payment.setStatus(PaymentStatus.ERROR);
            payment.setErrorMessage(log.getResponseMsg());
            logger.info("Payment {} marked as ERROR: {}", payment.getId(), log.getResponseMsg());
        }
    }

    private List<Payment> findStuckPayments(Instant cutoffTime) {
        return new ArrayList<>();
    }

    private TransactionLog findTransactionLog(String paymentId) {
        return null;
    }
}
