package com.zuora.aitest.payment;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {
    private String id;
    private String tenantId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private String errorMessage;
    private Integer totalItemsToApply;
    private Integer appliedItemsCount;
    private Instant createdDate;
    private Instant lastModifiedDate;

    public Payment() {
        this.createdDate = Instant.now();
        this.lastModifiedDate = Instant.now();
        this.status = PaymentStatus.PENDING;
        this.appliedItemsCount = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
        this.lastModifiedDate = Instant.now();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getTotalItemsToApply() {
        return totalItemsToApply;
    }

    public void setTotalItemsToApply(Integer totalItemsToApply) {
        this.totalItemsToApply = totalItemsToApply;
    }

    public Integer getAppliedItemsCount() {
        return appliedItemsCount;
    }

    public void setAppliedItemsCount(Integer appliedItemsCount) {
        this.appliedItemsCount = appliedItemsCount;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + id + '\'' +
                ", tenantId='" + tenantId + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", status=" + status +
                ", errorMessage='" + errorMessage + '\'' +
                ", totalItemsToApply=" + totalItemsToApply +
                ", appliedItemsCount=" + appliedItemsCount +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
}
