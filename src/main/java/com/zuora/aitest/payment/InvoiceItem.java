package com.zuora.aitest.payment;

import java.math.BigDecimal;

public class InvoiceItem {
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "InvoiceItem{" +
                "id='" + id + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}
