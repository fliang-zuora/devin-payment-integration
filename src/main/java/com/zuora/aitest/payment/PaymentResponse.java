package com.zuora.aitest.payment;

/**
 * Represents a payment response with transaction details and status.
 */
public class PaymentResponse {

    private String transactionId;
    private String status;
    private String message;
    private String timestamp;

    public PaymentResponse() {
        this.timestamp = java.time.Instant.now().toString();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "transactionId='" + transactionId + '\'' +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
