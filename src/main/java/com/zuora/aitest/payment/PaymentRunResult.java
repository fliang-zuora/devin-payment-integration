package com.zuora.aitest.payment;

import java.util.ArrayList;
import java.util.List;

public class PaymentRunResult {

    private String paymentRunId;
    private int processedCount;
    private int skippedCount;
    private int failedCount;
    private List<PaymentDetail> successfulPayments;
    private List<InvoiceDetail> skippedInvoices;
    private List<InvoiceDetail> failedInvoices;

    public PaymentRunResult(String paymentRunId) {
        this.paymentRunId = paymentRunId;
        this.successfulPayments = new ArrayList<>();
        this.skippedInvoices = new ArrayList<>();
        this.failedInvoices = new ArrayList<>();
    }

    public void addSuccessfulPayment(String invoiceId, String paymentId) {
        successfulPayments.add(new PaymentDetail(invoiceId, paymentId));
    }

    public void addSkippedInvoice(String invoiceId, String reason) {
        skippedInvoices.add(new InvoiceDetail(invoiceId, reason));
    }

    public void addFailedInvoice(String invoiceId, String reason) {
        failedInvoices.add(new InvoiceDetail(invoiceId, reason));
    }

    public String getPaymentRunId() {
        return paymentRunId;
    }

    public void setPaymentRunId(String paymentRunId) {
        this.paymentRunId = paymentRunId;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(int processedCount) {
        this.processedCount = processedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<PaymentDetail> getSuccessfulPayments() {
        return successfulPayments;
    }

    public List<InvoiceDetail> getSkippedInvoices() {
        return skippedInvoices;
    }

    public List<InvoiceDetail> getFailedInvoices() {
        return failedInvoices;
    }

    @Override
    public String toString() {
        return "PaymentRunResult{" +
                "paymentRunId='" + paymentRunId + '\'' +
                ", processedCount=" + processedCount +
                ", skippedCount=" + skippedCount +
                ", failedCount=" + failedCount +
                ", successfulPayments=" + successfulPayments.size() +
                ", skippedInvoices=" + skippedInvoices.size() +
                ", failedInvoices=" + failedInvoices.size() +
                '}';
    }

    public static class PaymentDetail {
        private String invoiceId;
        private String paymentId;

        public PaymentDetail(String invoiceId, String paymentId) {
            this.invoiceId = invoiceId;
            this.paymentId = paymentId;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public String getPaymentId() {
            return paymentId;
        }

        @Override
        public String toString() {
            return "PaymentDetail{" +
                    "invoiceId='" + invoiceId + '\'' +
                    ", paymentId='" + paymentId + '\'' +
                    '}';
        }
    }

    public static class InvoiceDetail {
        private String invoiceId;
        private String reason;

        public InvoiceDetail(String invoiceId, String reason) {
            this.invoiceId = invoiceId;
            this.reason = reason;
        }

        public String getInvoiceId() {
            return invoiceId;
        }

        public String getReason() {
            return reason;
        }

        @Override
        public String toString() {
            return "InvoiceDetail{" +
                    "invoiceId='" + invoiceId + '\'' +
                    ", reason='" + reason + '\'' +
                    '}';
        }
    }
}
