package com.zuora.aitest.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

public class PaymentRunService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRunService.class);
    private static final int MAX_SYNC_ITEMS = 15000;
    private static final int MAX_ASYNC_ITEMS = 50000;

    public void validateAndCreatePayment(PaymentRequest request) {
        int itemCount = calculateTotalItems(request);

        logger.info("Payment Run creating payment with {} items", itemCount);

        if (itemCount > MAX_ASYNC_ITEMS) {
            throw new PaymentValidationException(
                "Total invoice items (" + itemCount + ") exceeds maximum allowed (" + 
                MAX_ASYNC_ITEMS + ")");
        }

        if (itemCount > MAX_SYNC_ITEMS) {
            logger.warn("Payment has {} items (>{} threshold), routing to async allocation", 
                itemCount, MAX_SYNC_ITEMS);
            createPaymentWithAsyncAllocation(request, itemCount);
        } else {
            logger.info("Payment has {} items, using sync allocation", itemCount);
            createPaymentWithSyncAllocation(request);
        }
    }

    public void createPaymentWithTransactionLog(Payment payment) {
        logger.info("Creating payment {} with guaranteed transaction log", payment.getId());

        TransactionLog log = new TransactionLog();
        log.setId(UUID.randomUUID().toString());
        log.setPaymentId(payment.getId());
        log.setResponseCode("PENDING");
        log.setResponseMsg("Payment initiated, awaiting gateway response");
        log.setIdempotencyKey(UUID.randomUUID().toString());
        log.setCreatedDate(Instant.now());

        logger.info("Created transaction log {} for payment {}", log.getId(), payment.getId());

        processPaymentGateway(payment, log);
    }

    private void createPaymentWithSyncAllocation(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PROCESSING);

        createPaymentWithTransactionLog(payment);

        logger.info("Payment {} created with sync allocation", payment.getId());
    }

    private void createPaymentWithAsyncAllocation(PaymentRequest request, int itemCount) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.APPLYING);
        payment.setTotalItemsToApply(itemCount);
        payment.setAppliedItemsCount(0);

        createPaymentWithTransactionLog(payment);

        logger.info("Payment {} created with async allocation for {} items", 
            payment.getId(), itemCount);
    }

    private void processPaymentGateway(Payment payment, TransactionLog log) {
        logger.info("Processing payment {} through gateway", payment.getId());
    }

    private int calculateTotalItems(PaymentRequest request) {
        return 0;
    }
}
