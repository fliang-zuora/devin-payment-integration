package com.zuora.aitest.payment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PaymentRunService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRunService.class);

    private final PaymentService paymentService;
    private final RunPaymentRepository runPaymentRepository;

    public PaymentRunService(PaymentService paymentService, RunPaymentRepository runPaymentRepository) {
        this.paymentService = paymentService;
        this.runPaymentRepository = runPaymentRepository;
    }

    public PaymentRunResult processPaymentRun(String paymentRunId, List<Invoice> invoices) {
        logger.info("Starting payment run {} with {} invoices", paymentRunId, invoices.size());

        PaymentRunResult result = new PaymentRunResult(paymentRunId);
        int processedCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (Invoice invoice : invoices) {
            logger.info("Processing invoice {} in payment run {}", invoice.getId(), paymentRunId);

            if (runPaymentRepository.isInvoiceProcessedInRun(paymentRunId, invoice.getId())) {
                logger.warn("Invoice {} is already processed in payment run {}. Skipping.", 
                           invoice.getId(), paymentRunId);
                result.addSkippedInvoice(invoice.getId(), "Invoice already processed in this payment run");
                skippedCount++;
                continue;
            }

            try {
                PaymentRequest paymentRequest = createPaymentRequestFromInvoice(invoice);
                PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

                RunPayment runPayment = new RunPayment(paymentRunId, invoice.getId());
                runPayment.setPaymentId(paymentResponse.getTransactionId());
                runPayment.setStatus(paymentResponse.getStatus());
                runPaymentRepository.save(runPayment);

                if ("SUCCESS".equals(paymentResponse.getStatus())) {
                    logger.info("Successfully processed invoice {} with payment ID {}", 
                               invoice.getId(), paymentResponse.getTransactionId());
                    result.addSuccessfulPayment(invoice.getId(), paymentResponse.getTransactionId());
                    processedCount++;
                } else {
                    logger.error("Failed to process invoice {}: {}", 
                                invoice.getId(), paymentResponse.getMessage());
                    result.addFailedInvoice(invoice.getId(), paymentResponse.getMessage());
                    failedCount++;
                }

            } catch (Exception e) {
                logger.error("Error processing invoice {} in payment run {}", 
                            invoice.getId(), paymentRunId, e);
                result.addFailedInvoice(invoice.getId(), "Exception: " + e.getMessage());
                failedCount++;
            }
        }

        result.setProcessedCount(processedCount);
        result.setSkippedCount(skippedCount);
        result.setFailedCount(failedCount);

        logger.info("Payment run {} completed. Processed: {}, Skipped: {}, Failed: {}", 
                   paymentRunId, processedCount, skippedCount, failedCount);

        return result;
    }

    private PaymentRequest createPaymentRequestFromInvoice(Invoice invoice) {
        PaymentRequest request = new PaymentRequest();
        request.setAmount(invoice.getBalance());
        request.setCurrency(invoice.getCurrency());
        request.setCustomerEmail("customer@example.com");
        request.setPaymentMethod("CREDIT_CARD");
        request.setDescription("Payment for invoice " + invoice.getInvoiceNumber());
        return request;
    }
}
