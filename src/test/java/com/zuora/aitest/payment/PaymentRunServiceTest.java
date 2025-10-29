package com.zuora.aitest.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentRunServiceTest {

    private PaymentRunService paymentRunService;
    private PaymentService paymentService;
    private RunPaymentRepository runPaymentRepository;

    @BeforeEach
    public void setUp() {
        paymentService = new PaymentService();
        runPaymentRepository = new RunPaymentRepository();
        paymentRunService = new PaymentRunService(paymentService, runPaymentRepository);
    }

    @Test
    public void testProcessPaymentRun_WithNewInvoices() {
        String paymentRunId = "PR-001";
        List<Invoice> invoices = createTestInvoices(3);

        PaymentRunResult result = paymentRunService.processPaymentRun(paymentRunId, invoices);

        assertNotNull(result);
        assertEquals(paymentRunId, result.getPaymentRunId());
        assertEquals(3, result.getProcessedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(0, result.getFailedCount());
        assertEquals(3, result.getSuccessfulPayments().size());
    }

    @Test
    public void testProcessPaymentRun_WithDuplicateInvoice() {
        String paymentRunId = "PR-002";
        List<Invoice> invoices = createTestInvoices(3);

        PaymentRunResult firstRun = paymentRunService.processPaymentRun(paymentRunId, invoices);
        assertEquals(3, firstRun.getProcessedCount());
        assertEquals(0, firstRun.getSkippedCount());

        PaymentRunResult secondRun = paymentRunService.processPaymentRun(paymentRunId, invoices);
        assertEquals(0, secondRun.getProcessedCount());
        assertEquals(3, secondRun.getSkippedCount());
        assertEquals(0, secondRun.getFailedCount());
        assertEquals(3, secondRun.getSkippedInvoices().size());
    }

    @Test
    public void testProcessPaymentRun_WithPartialDuplicates() {
        String paymentRunId = "PR-003";
        List<Invoice> firstBatch = createTestInvoices(2);
        List<Invoice> secondBatch = Arrays.asList(
            firstBatch.get(0),
            firstBatch.get(1),
            createInvoice("INV-003", "ACC-003", new BigDecimal("300.00"))
        );

        PaymentRunResult firstRun = paymentRunService.processPaymentRun(paymentRunId, firstBatch);
        assertEquals(2, firstRun.getProcessedCount());
        assertEquals(0, firstRun.getSkippedCount());

        PaymentRunResult secondRun = paymentRunService.processPaymentRun(paymentRunId, secondBatch);
        assertEquals(1, secondRun.getProcessedCount());
        assertEquals(2, secondRun.getSkippedCount());
        assertEquals(0, secondRun.getFailedCount());
    }

    @Test
    public void testProcessPaymentRun_DifferentPaymentRuns() {
        String paymentRunId1 = "PR-004";
        String paymentRunId2 = "PR-005";
        List<Invoice> invoices = createTestInvoices(2);

        PaymentRunResult result1 = paymentRunService.processPaymentRun(paymentRunId1, invoices);
        assertEquals(2, result1.getProcessedCount());
        assertEquals(0, result1.getSkippedCount());

        PaymentRunResult result2 = paymentRunService.processPaymentRun(paymentRunId2, invoices);
        assertEquals(2, result2.getProcessedCount());
        assertEquals(0, result2.getSkippedCount());
    }

    @Test
    public void testProcessPaymentRun_EmptyInvoiceList() {
        String paymentRunId = "PR-006";
        List<Invoice> invoices = Arrays.asList();

        PaymentRunResult result = paymentRunService.processPaymentRun(paymentRunId, invoices);

        assertNotNull(result);
        assertEquals(0, result.getProcessedCount());
        assertEquals(0, result.getSkippedCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    public void testRunPaymentRepository_IsInvoiceProcessedInRun() {
        String paymentRunId = "PR-007";
        String invoiceId = "INV-001";

        assertFalse(runPaymentRepository.isInvoiceProcessedInRun(paymentRunId, invoiceId));

        RunPayment runPayment = new RunPayment(paymentRunId, invoiceId);
        runPaymentRepository.save(runPayment);

        assertTrue(runPaymentRepository.isInvoiceProcessedInRun(paymentRunId, invoiceId));
    }

    @Test
    public void testRunPaymentRepository_FindByPaymentRunIdAndInvoiceId() {
        String paymentRunId = "PR-008";
        String invoiceId = "INV-001";

        RunPayment runPayment = new RunPayment(paymentRunId, invoiceId);
        runPayment.setPaymentId("PAY-001");
        runPayment.setStatus("SUCCESS");
        runPaymentRepository.save(runPayment);

        RunPayment found = runPaymentRepository.findByPaymentRunIdAndInvoiceId(paymentRunId, invoiceId);
        assertNotNull(found);
        assertEquals(paymentRunId, found.getPaymentRunId());
        assertEquals(invoiceId, found.getInvoiceId());
        assertEquals("PAY-001", found.getPaymentId());
        assertEquals("SUCCESS", found.getStatus());
    }

    @Test
    public void testRunPaymentRepository_FindByPaymentRunId() {
        String paymentRunId = "PR-009";
        
        RunPayment rp1 = new RunPayment(paymentRunId, "INV-001");
        RunPayment rp2 = new RunPayment(paymentRunId, "INV-002");
        RunPayment rp3 = new RunPayment("PR-010", "INV-003");
        
        runPaymentRepository.save(rp1);
        runPaymentRepository.save(rp2);
        runPaymentRepository.save(rp3);

        List<RunPayment> results = runPaymentRepository.findByPaymentRunId(paymentRunId);
        assertEquals(2, results.size());
    }

    private List<Invoice> createTestInvoices(int count) {
        Invoice[] invoices = new Invoice[count];
        for (int i = 0; i < count; i++) {
            invoices[i] = createInvoice(
                "INV-" + String.format("%03d", i + 1),
                "ACC-" + String.format("%03d", i + 1),
                new BigDecimal((i + 1) * 100 + ".00")
            );
        }
        return Arrays.asList(invoices);
    }

    private Invoice createInvoice(String id, String accountId, BigDecimal amount) {
        Invoice invoice = new Invoice(id, "INV-NUM-" + id, accountId, amount, amount, "USD");
        invoice.setStatus("Posted");
        return invoice;
    }
}
