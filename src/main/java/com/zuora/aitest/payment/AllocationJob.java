package com.zuora.aitest.payment;

import java.time.Instant;

public class AllocationJob {
    private String id;
    private String paymentId;
    private JobStatus status;
    private Integer totalBatches;
    private Integer completedBatches;
    private Instant createdDate;
    private Instant lastModifiedDate;

    public AllocationJob() {
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

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
        this.lastModifiedDate = Instant.now();
    }

    public Integer getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(Integer totalBatches) {
        this.totalBatches = totalBatches;
    }

    public Integer getCompletedBatches() {
        return completedBatches;
    }

    public void setCompletedBatches(Integer completedBatches) {
        this.completedBatches = completedBatches;
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
        return "AllocationJob{" +
                "id='" + id + '\'' +
                ", paymentId='" + paymentId + '\'' +
                ", status=" + status +
                ", totalBatches=" + totalBatches +
                ", completedBatches=" + completedBatches +
                ", createdDate=" + createdDate +
                ", lastModifiedDate=" + lastModifiedDate +
                '}';
    }
}
