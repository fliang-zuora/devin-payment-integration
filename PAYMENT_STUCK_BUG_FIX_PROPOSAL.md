# Payment Stuck in Processing Status - Bug Fix Proposal

## Executive Summary

This document proposes a comprehensive fix for the issue where payments created via Payment Run get stuck in "Processing" status when dealing with large invoice item volumes (>15,000 items), causing gateway timeouts on subsequent sync operations.

## Problem Analysis

### Root Causes

1. **Missing Transaction Log Guarantees**: Payments can be created without valid transaction logs (blank response_code/response_msg), leaving them indefinitely in "Processing" status.

2. **Scale Mismatch**: Payment Run allows massive invoice item counts (38,825+ items) but UI/API sync operations have 2-minute timeouts and cannot handle such volumes.

3. **No Validation on Payment Run**: The 15,000 item limit applies only to sync API/UI operations, not to Payment Run which bypasses this check.

4. **Synchronous View/Update Operations**: All payment view/update operations attempt to load all invoice items synchronously, causing 504 gateway timeouts for large payments.

5. **Scheduler Dependency**: RefreshStuckPaymentStatusSchedulerAction relies on transaction logs created within 5 days, which doesn't help when logs are missing or blank.

## Proposed Solution

### Phase 0: Immediate Hotfix (Critical)

#### 1. Guarantee Transaction Log Creation

**Objective**: Ensure every payment has a valid transaction log, even if pending.

**Implementation**:
- Create transaction log with `response_code = "PENDING"` before gateway dispatch
- Use idempotency keys to prevent duplicate logs
- Never leave response_code/response_msg blank

**Code Changes**:
```java
// In PaymentRunService.java (or equivalent)
public void createPaymentWithTransactionLog(Payment payment) {
    // Create transaction log BEFORE gateway call
    TransactionLog log = new TransactionLog();
    log.setPaymentId(payment.getId());
    log.setResponseCode("PENDING");
    log.setResponseMsg("Payment initiated, awaiting gateway response");
    log.setIdempotencyKey(UUID.randomUUID().toString());
    log.setCreatedDate(Instant.now());
    transactionLogRepository.save(log);
    
    // Then proceed with gateway call
    processPaymentGateway(payment, log);
}
```

#### 2. Add Timeout-Based Status Transition

**Objective**: Prevent payments from staying in "Processing" indefinitely.

**Implementation**:
- Update RefreshStuckPaymentStatusSchedulerAction to handle missing/blank logs
- Transition to "Error" status after configurable timeout (15-60 minutes)

**Code Changes**:
```java
// In RefreshStuckPaymentStatusSchedulerAction.java
public void processStuckPayments() {
    Instant cutoffTime = Instant.now().minus(15, ChronoUnit.MINUTES);
    
    // Find payments stuck in Processing
    List<Payment> stuckPayments = paymentRepository.findByStatusAndLastModifiedBefore(
        PaymentStatus.PROCESSING, cutoffTime);
    
    for (Payment payment : stuckPayments) {
        TransactionLog log = transactionLogRepository.findByPaymentId(payment.getId());
        
        if (log == null || isBlankLog(log)) {
            // No valid log after timeout - transition to Error
            payment.setStatus(PaymentStatus.ERROR);
            payment.setErrorMessage("TIMED_OUT_WAITING_FOR_TRANSACTION_LOG");
            paymentRepository.save(payment);
            logger.error("Payment {} timed out waiting for transaction log", payment.getId());
        } else if ("PENDING".equals(log.getResponseCode())) {
            // Still pending after timeout - check gateway status or error out
            checkGatewayStatusOrError(payment, log);
        } else {
            // Valid log exists - process normally
            updatePaymentFromLog(payment, log);
        }
    }
}

private boolean isBlankLog(TransactionLog log) {
    return log.getResponseCode() == null || log.getResponseCode().trim().isEmpty();
}
```

### Phase 1: Scalability Improvements (High Priority)

#### 3. Add Invoice Item Count Validation to Payment Run

**Objective**: Prevent creation of payments with excessive invoice items.

**Implementation**:
- Add configurable threshold (default: 15,000 items)
- Route large payments to async allocation pipeline
- Emit metrics and alerts for large item counts

**Code Changes**:
```java
// In PaymentRunService.java
private static final int MAX_SYNC_ITEMS = 15000;
private static final int MAX_ASYNC_ITEMS = 50000; // Hard limit

public void validateAndCreatePayment(PaymentRequest request) {
    int itemCount = calculateTotalItems(request);
    
    // Emit metric
    metricsService.recordItemCount("payment.run.item.count", itemCount);
    
    if (itemCount > MAX_ASYNC_ITEMS) {
        throw new PaymentValidationException(
            "Total invoice items (" + itemCount + ") exceeds maximum allowed (" + 
            MAX_ASYNC_ITEMS + ")");
    }
    
    if (itemCount > MAX_SYNC_ITEMS) {
        // Route to async allocation pipeline
        logger.warn("Payment has {} items, routing to async allocation", itemCount);
        createPaymentWithAsyncAllocation(request, itemCount);
    } else {
        // Normal sync processing
        createPaymentWithSyncAllocation(request);
    }
}

private int calculateTotalItems(PaymentRequest request) {
    int count = 0;
    for (Invoice invoice : request.getInvoices()) {
        count += invoiceItemRepository.countByInvoiceId(invoice.getId());
    }
    for (DebitMemo memo : request.getDebitMemos()) {
        count += debitMemoItemRepository.countByDebitMemoId(memo.getId());
    }
    return count;
}
```

#### 4. Implement Async Allocation Pipeline

**Objective**: Handle large invoice item allocations asynchronously in batches.

**Implementation**:
- Process allocations in batches of 1,000-2,000 items
- Use idempotency to support retries
- Store progress metadata for resumability

**Code Changes**:
```java
// New class: AsyncAllocationService.java
public class AsyncAllocationService {
    private static final int BATCH_SIZE = 1000;
    
    public void createPaymentWithAsyncAllocation(PaymentRequest request, int totalItems) {
        // Create payment in "Applying" status
        Payment payment = new Payment();
        payment.setStatus(PaymentStatus.APPLYING);
        payment.setTotalItemsToApply(totalItems);
        payment.setAppliedItemsCount(0);
        paymentRepository.save(payment);
        
        // Create transaction log
        createPendingTransactionLog(payment);
        
        // Submit async job
        AllocationJob job = new AllocationJob();
        job.setPaymentId(payment.getId());
        job.setStatus(JobStatus.PENDING);
        job.setTotalBatches((totalItems + BATCH_SIZE - 1) / BATCH_SIZE);
        job.setCompletedBatches(0);
        allocationJobRepository.save(job);
        
        // Queue for BET processing
        jobQueue.submit(job);
    }
    
    public void processAllocationBatch(AllocationJob job, int batchNumber) {
        Payment payment = paymentRepository.findById(job.getPaymentId());
        
        // Get batch of items to process
        int offset = batchNumber * BATCH_SIZE;
        List<InvoiceItem> items = getItemsForBatch(payment, offset, BATCH_SIZE);
        
        // Process batch with idempotency
        for (InvoiceItem item : items) {
            applyItemToPayment(payment, item);
        }
        
        // Update progress
        job.setCompletedBatches(batchNumber + 1);
        payment.setAppliedItemsCount(payment.getAppliedItemsCount() + items.size());
        
        if (job.getCompletedBatches() >= job.getTotalBatches()) {
            // All batches complete
            job.setStatus(JobStatus.COMPLETED);
            payment.setStatus(PaymentStatus.PROCESSED);
        }
        
        allocationJobRepository.save(job);
        paymentRepository.save(payment);
    }
}
```

#### 5. Add Paginated View Endpoints

**Objective**: Allow viewing large payments without timeouts.

**Implementation**:
- Add summary endpoint returning counts and totals only
- Add paginated endpoint for detailed items
- Use keyset pagination for efficiency

**Code Changes**:
```java
// In PaymentController.java
@GetMapping("/payments/{id}")
public PaymentResponse getPayment(
        @PathVariable String id,
        @RequestParam(defaultValue = "false") boolean summaryOnly) {
    
    Payment payment = paymentRepository.findById(id);
    
    if (summaryOnly) {
        return buildPaymentSummary(payment);
    } else {
        // Check item count
        int itemCount = getAppliedItemCount(payment);
        if (itemCount > 1000) {
            // Return summary with pagination hint
            PaymentResponse response = buildPaymentSummary(payment);
            response.setMessage("Payment has " + itemCount + 
                " items. Use /payments/{id}/applications endpoint for details.");
            return response;
        } else {
            return buildFullPaymentResponse(payment);
        }
    }
}

@GetMapping("/payments/{id}/applications")
public PagedResponse<ApplicationItem> getPaymentApplications(
        @PathVariable String id,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "100") int size) {
    
    // Cap page size
    size = Math.min(size, 500);
    
    Payment payment = paymentRepository.findById(id);
    
    // Use keyset pagination for efficiency
    Page<ApplicationItem> items = applicationRepository
        .findBySourceTransactionIdAndTenantIdAndDeletedFalse(
            payment.getId(), 
            payment.getTenantId(),
            PageRequest.of(page, size));
    
    return new PagedResponse<>(items);
}

private PaymentResponse buildPaymentSummary(Payment payment) {
    PaymentResponse response = new PaymentResponse();
    response.setTransactionId(payment.getId());
    response.setStatus(payment.getStatus().toString());
    response.setAmount(payment.getAmount());
    
    // Add summary counts
    int itemCount = getAppliedItemCount(payment);
    int invoiceCount = getAppliedInvoiceCount(payment);
    
    response.setSummary(new PaymentSummary(itemCount, invoiceCount));
    return response;
}
```

### Phase 2: Long-term Improvements (Medium Priority)

#### 6. Add Async Update Endpoint

**Objective**: Allow updates to large payments without timeouts.

**Code Changes**:
```java
// In PaymentController.java
@PostMapping("/payments/{id}/update-async")
public AsyncJobResponse updatePaymentAsync(
        @PathVariable String id,
        @RequestBody PaymentUpdateRequest request) {
    
    Payment payment = paymentRepository.findById(id);
    
    // Create async job
    UpdateJob job = new UpdateJob();
    job.setPaymentId(id);
    job.setStatus(JobStatus.PENDING);
    job.setUpdateRequest(request);
    updateJobRepository.save(job);
    
    // Queue for processing
    jobQueue.submit(job);
    
    return new AsyncJobResponse(job.getId(), "Job submitted for processing");
}

@GetMapping("/jobs/{jobId}")
public JobStatusResponse getJobStatus(@PathVariable String jobId) {
    UpdateJob job = updateJobRepository.findById(jobId);
    return new JobStatusResponse(job.getStatus(), job.getProgress(), job.getMessage());
}
```

#### 7. Add Database Indexes

**Objective**: Optimize queries for large payment allocations.

**SQL Changes**:
```sql
-- Add compound index for efficient queries
CREATE INDEX idx_ar_txn_item_app_source_tenant_deleted 
ON ar_transaction_item_application(source_transaction_id, tenant_id, deleted, id);

-- Add index for keyset pagination
CREATE INDEX idx_ar_txn_item_app_keyset 
ON ar_transaction_item_application(source_transaction_id, id);

-- Add index for scheduler queries
CREATE INDEX idx_payment_status_modified 
ON payment(status, last_modified_date);
```

#### 8. Add Observability and Alerts

**Objective**: Detect and alert on problematic patterns.

**Implementation**:
```java
// In PaymentMetricsService.java
public void recordPaymentCreation(Payment payment, int itemCount) {
    metricsService.increment("payment.created");
    metricsService.recordHistogram("payment.item.count", itemCount);
    
    if (itemCount > 15000) {
        metricsService.increment("payment.large.item.count");
        logger.warn("Large payment created: {} with {} items", 
            payment.getId(), itemCount);
    }
}

public void recordStuckPayment(Payment payment) {
    metricsService.increment("payment.stuck.processing");
    alertService.sendAlert(
        "Payment stuck in Processing",
        "Payment " + payment.getId() + " has been in Processing for > 15 minutes"
    );
}
```

#### 9. Add Admin Tooling

**Objective**: Provide internal tools to fix stuck payments without manual data fixes.

**Code Changes**:
```java
// In AdminPaymentController.java (secured endpoint)
@PostMapping("/admin/payments/{id}/fix-transaction-log")
@PreAuthorize("hasRole('ADMIN')")
public AdminResponse fixTransactionLog(
        @PathVariable String id,
        @RequestBody TransactionLogFixRequest request,
        @AuthenticationPrincipal User admin) {
    
    Payment payment = paymentRepository.findById(id);
    
    // Audit log
    auditService.log(
        "TRANSACTION_LOG_FIX",
        admin.getUsername(),
        payment.getId(),
        request.getReason()
    );
    
    // Create or update transaction log
    TransactionLog log = transactionLogRepository.findByPaymentId(id);
    if (log == null) {
        log = new TransactionLog();
        log.setPaymentId(id);
    }
    
    log.setResponseCode(request.getResponseCode());
    log.setResponseMsg(request.getResponseMsg());
    log.setGatewayReferenceId(request.getGatewayReferenceId());
    transactionLogRepository.save(log);
    
    return new AdminResponse("Transaction log fixed successfully");
}
```

## Configuration

### Configurable Parameters

```yaml
payment:
  validation:
    max_sync_items: 15000
    max_async_items: 50000
    
  allocation:
    batch_size: 1000
    
  scheduler:
    stuck_payment_timeout_minutes: 15
    check_interval_minutes: 60
    
  pagination:
    default_page_size: 100
    max_page_size: 500
```

## Rollout Plan

### Phase 0 (Week 1)
- Implement transaction log guarantees
- Update scheduler to handle missing logs
- Deploy with feature flag
- Monitor metrics

### Phase 1 (Week 2-3)
- Add item count validation to Payment Run
- Implement async allocation pipeline
- Add paginated view endpoints
- Deploy incrementally with feature flags

### Phase 2 (Week 4-6)
- Add async update endpoints
- Implement database indexes
- Add observability and alerts
- Deploy admin tooling
- Update documentation

## Testing Strategy

1. **Unit Tests**: Test each component in isolation
2. **Integration Tests**: Test end-to-end flows with various item counts
3. **Load Tests**: Simulate payments with 50k+ items
4. **Shadow Mode**: Run new logic alongside old logic, compare results
5. **Canary Deployment**: Roll out to small percentage of tenants first

## Monitoring and Alerts

### Key Metrics
- `payment.created` - Total payments created
- `payment.item.count` - Distribution of item counts
- `payment.large.item.count` - Payments with >15k items
- `payment.stuck.processing` - Payments stuck in Processing
- `payment.allocation.batch.duration` - Batch processing time
- `payment.view.timeout` - View operation timeouts

### Alerts
- Payment stuck in Processing for >15 minutes
- Payment created with >50k items
- View/update operation timeout rate >1%
- Allocation job failure rate >5%

## Documentation Updates

1. Update Knowledge Center article about 15k item limit
2. Document async allocation behavior for Payment Run
3. Document paginated endpoints for large payments
4. Add troubleshooting guide for stuck payments
5. Document admin tooling usage

## Risks and Mitigations

### Risk 1: Breaking Existing Behavior
**Mitigation**: Use feature flags, shadow mode, canary deployment

### Risk 2: Performance Impact
**Mitigation**: Add database indexes, optimize queries, load test

### Risk 3: Data Inconsistency
**Mitigation**: Use idempotency keys, transactional updates, audit logs

### Risk 4: Scheduler Changes
**Mitigation**: Coordinate with PAY/Cash App teams, gradual rollout

## Success Criteria

1. Zero payments stuck in Processing for >1 hour
2. Zero 504 timeouts on payment view operations
3. All payments with >15k items use async allocation
4. 99.9% of payments have valid transaction logs
5. Average payment view response time <500ms

## Next Steps

1. Review proposal with PAY and Cash App teams
2. Get alignment on phasing and timelines
3. Create detailed implementation tickets
4. Set up feature flags and monitoring
5. Begin Phase 0 implementation
