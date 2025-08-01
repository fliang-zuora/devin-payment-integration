package com.zuora.aitest.payment;

import java.math.BigDecimal;

/**
 * Represents a payment request with all necessary details.
 */
public class PaymentRequest {

    private BigDecimal amount;
    private String currency;
    private String customerEmail;
    private String paymentMethod;
    private String description;

    public PaymentRequest() {}

    public PaymentRequest(BigDecimal amount, String currency, String customerEmail,
                         String paymentMethod, String description) {
        this.amount = amount;
        this.currency = currency;
        this.customerEmail = customerEmail;
        this.paymentMethod = paymentMethod;
        this.description = description;
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

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "PaymentRequest{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
