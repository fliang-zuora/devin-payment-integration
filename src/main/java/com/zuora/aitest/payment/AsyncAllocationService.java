package com.zuora.aitest.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AsyncAllocationService {

    private static final Logger logger = LoggerFactory.getLogger(AsyncAllocationService.class);
    private static final int BATCH_SIZE = 1000;

    public void createPaymentWithAsyncAllocation(PaymentRequest request, int totalItems) {
        logger.info("Creating payment with async allocation for {} items", totalItems);

        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setStatus(PaymentStatus.APPLYING);
        payment.setTotalItemsToApply(totalItems);
        payment.setAppliedItemsCount(0);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());

        createPendingTransactionLog(payment);

        AllocationJob job = new AllocationJob();
        job.setId(UUID.randomUUID().toString());
        job.setPaymentId(payment.getId());
        job.setStatus(JobStatus.PENDING);
        job.setTotalBatches((totalItems + BATCH_SIZE - 1) / BATCH_SIZE);
        job.setCompletedBatches(0);

        logger.info("Created allocation job {} with {} batches for payment {}",
            job.getId(), job.getTotalBatches(), payment.getId());

        submitJobToQueue(job);
    }

    public void processAllocationBatch(AllocationJob job, int batchNumber) {
        logger.info("Processing batch {} of {} for job {}",
            batchNumber, job.getTotalBatches(), job.getId());

        Payment payment = findPayment(job.getPaymentId());

        int offset = batchNumber * BATCH_SIZE;
        List<InvoiceItem> items = getItemsForBatch(payment, offset, BATCH_SIZE);

        logger.info("Processing {} items in batch {}", items.size(), batchNumber);

        for (InvoiceItem item : items) {
            applyItemToPayment(payment, item);
        }

        job.setCompletedBatches(batchNumber + 1);
        payment.setAppliedItemsCount(payment.getAppliedItemsCount() + items.size());

        if (job.getCompletedBatches() >= job.getTotalBatches()) {
            logger.info("All batches completed for job {}, marking payment {} as PROCESSED",
                job.getId(), payment.getId());

            job.setStatus(JobStatus.COMPLETED);
            payment.setStatus(PaymentStatus.PROCESSED);
        } else {
            logger.info("Batch {} completed, {} batches remaining",
                batchNumber, job.getTotalBatches() - job.getCompletedBatches());
        }
    }

    private void createPendingTransactionLog(Payment payment) {
        TransactionLog log = new TransactionLog();
        log.setId(UUID.randomUUID().toString());
        log.setPaymentId(payment.getId());
        log.setResponseCode("PENDING");
        log.setResponseMsg("Payment initiated with async allocation");
        log.setIdempotencyKey(UUID.randomUUID().toString());

        logger.info("Created pending transaction log {} for payment {}", log.getId(), payment.getId());
    }

    private void applyItemToPayment(Payment payment, InvoiceItem item) {
        logger.debug("Applying item {} to payment {}", item.getId(), payment.getId());
    }

    private List<InvoiceItem> getItemsForBatch(Payment payment, int offset, int batchSize) {
        return new ArrayList<>();
    }

    private Payment findPayment(String paymentId) {
        return new Payment();
    }

    private void submitJobToQueue(AllocationJob job) {
        logger.info("Submitting job {} to queue", job.getId());
    }
}
