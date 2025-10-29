package com.zuora.aitest.payment;

import java.time.Instant;

public class TransactionLog {
    private String id;
    private String paymentId;
    private String responseCode;
    private String responseMsg;
    private String gatewayReferenceId;
    private String idempotencyKey;
    private Instant createdDate;
    private Instant lastModifiedDate;

    public TransactionLog() {
        this.createdDate = Instant.now();
        this.lastModifiedDate = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getGatewayReferenceId() {
        return gatewayReferenceId;
    }

    public void setGatewayReferenceId(String gatewayReferenceId) {
        this.gatewayReferenceId = gatewayReferenceId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
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

    public boolean isBlank() {
        return responseCode == null || responseCode.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "TransactionLog{" +
                "id='" + id + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", responseMsg='" + responseMsg + '\'' +
                ", gatewayReferenceId='" + gatewayReferenceId + '\'' +
                ", idempotencyKey='" + idempotencyKey + '\'' +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
}
