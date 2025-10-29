package com.zuora.aitest.payment;

import java.time.Instant;

public class RunPayment {

    private String id;
    private String paymentRunId;
    private String invoiceId;
    private String paymentId;
    private String status;
    private Instant createdDate;
    private Instant updatedDate;

    public RunPayment() {}

    public RunPayment(String paymentRunId, String invoiceId) {
        this.paymentRunId = paymentRunId;
        this.invoiceId = invoiceId;
        this.createdDate = Instant.now();
        this.updatedDate = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaymentRunId() {
        return paymentRunId;
    }

    public void setPaymentRunId(String paymentRunId) {
        this.paymentRunId = paymentRunId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public String toString() {
        return "RunPayment{" +
                "id='" + id + '\'' +
                ", paymentRunId='" + paymentRunId + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", status='" + status + '\'' +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
