package com.zuora.aitest.payment;

import java.math.BigDecimal;
import java.time.Instant;

public class Invoice {

    private String id;
    private String invoiceNumber;
    private String accountId;
    private BigDecimal amount;
    private BigDecimal balance;
    private String currency;
    private String status;
    private Instant invoiceDate;
    private Instant dueDate;

    public Invoice() {}

    public Invoice(String id, String invoiceNumber, String accountId, BigDecimal amount, BigDecimal balance, String currency) {
        this.id = id;
        this.invoiceNumber = invoiceNumber;
        this.accountId = accountId;
        this.amount = amount;
        this.balance = balance;
        this.currency = currency;
        this.invoiceDate = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Instant invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public void setDueDate(Instant dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "id='" + id + '\'' +
                ", invoiceNumber='" + invoiceNumber + '\'' +
                ", accountId='" + accountId + '\'' +
                ", amount=" + amount +
                ", balance=" + balance +
                ", currency='" + currency + '\'' +
                ", status='" + status + '\'' +
                ", invoiceDate=" + invoiceDate +
                ", dueDate=" + dueDate +
                '}';
    }
}
