package com.zuora.aitest.payment;

public class PaymentSummary {
    private int totalItems;
    private int totalInvoices;
    private String message;

    public PaymentSummary(int totalItems, int totalInvoices) {
        this.totalItems = totalItems;
        this.totalInvoices = totalInvoices;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalInvoices() {
        return totalInvoices;
    }

    public void setTotalInvoices(int totalInvoices) {
        this.totalInvoices = totalInvoices;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PaymentSummary{" +
                "totalItems=" + totalItems +
                ", totalInvoices=" + totalInvoices +
                ", message='" + message + '\'' +
                '}';
    }
}
