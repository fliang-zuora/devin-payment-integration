# Implementation Guide for Payment Stuck Bug Fix

## Overview

This guide provides step-by-step instructions for implementing the fix for payments getting stuck in "Processing" status with large invoice item volumes.

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Database access with migration permissions
- Feature flag system configured

## Implementation Phases

### Phase 0: Immediate Hotfix (Week 1)

#### Step 1: Deploy Database Indexes

```bash
# Run the migration script
psql -U <username> -d <database> -f database/migrations/001_add_payment_indexes.sql
```

#### Step 2: Deploy Transaction Log Guarantees

1. Deploy `TransactionLog.java` class
2. Deploy `PaymentRunService.java` with `createPaymentWithTransactionLog()` method
3. Enable feature flag: `payment.guaranteed_transaction_log.enabled=true`
4. Monitor logs for transaction log creation

#### Step 3: Deploy Scheduler Updates

1. Deploy `RefreshStuckPaymentStatusSchedulerAction.java`
2. Configure timeout: `payment.scheduler.stuck_payment_timeout_minutes=15`
3. Enable feature flag: `payment.scheduler.enhanced_stuck_detection.enabled=true`
4. Monitor metrics: `payment.stuck.processing`

#### Step 4: Validation

- Verify no payments stuck in Processing for >15 minutes
- Check transaction logs are created for all new payments
- Monitor error logs for any issues

### Phase 1: Scalability Improvements (Week 2-3)

#### Step 1: Deploy Async Allocation Service

1. Deploy `AsyncAllocationService.java`
2. Deploy `AllocationJob.java` and `JobStatus.java`
3. Configure: `payment.allocation.batch_size=1000`
4. Enable feature flag: `payment.async_allocation.enabled=true`

#### Step 2: Deploy Payment Run Validation

1. Deploy updated `PaymentRunService.java` with validation
2. Configure thresholds in `application.yml`
3. Enable feature flag: `payment.run.item_validation.enabled=true`
4. Monitor metrics: `payment.large.item.count`

#### Step 3: Deploy Paginated Endpoints

1. Deploy paginated view endpoints
2. Update UI to use summary view by default
3. Enable feature flag: `payment.paginated_view.enabled=true`
4. Test with payments having >1000 items

#### Step 4: Validation

- Create test payment with 20,000 items via Payment Run
- Verify it routes to async allocation
- Verify view endpoint returns summary quickly
- Check allocation job completes successfully

### Phase 2: Long-term Improvements (Week 4-6)

#### Step 1: Deploy Async Update Endpoints

1. Deploy async update endpoints
2. Update UI to show job status
3. Enable feature flag: `payment.async_update.enabled=true`

#### Step 2: Deploy Observability

1. Add metrics collection
2. Configure alerts in monitoring system
3. Set up dashboards

#### Step 3: Deploy Admin Tooling

1. Deploy admin endpoints (secured)
2. Document usage in internal wiki
3. Train support team

#### Step 4: Update Documentation

1. Update Knowledge Center article
2. Add API documentation
3. Create troubleshooting guide

## Configuration

### Feature Flags

```yaml
payment:
  guaranteed_transaction_log:
    enabled: true
  
  scheduler:
    enhanced_stuck_detection:
      enabled: true
  
  run:
    item_validation:
      enabled: true
  
  async_allocation:
    enabled: true
  
  paginated_view:
    enabled: true
  
  async_update:
    enabled: true
```

### Application Configuration

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

## Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
mvn verify -P integration-tests
```

### Load Tests

```bash
# Test with 50,000 items
./scripts/load-test-large-payment.sh 50000
```

## Monitoring

### Key Metrics to Watch

- `payment.created` - Total payments created
- `payment.item.count` - Distribution of item counts
- `payment.large.item.count` - Payments with >15k items
- `payment.stuck.processing` - Payments stuck in Processing
- `payment.allocation.batch.duration` - Batch processing time
- `payment.view.timeout` - View operation timeouts

### Alerts to Configure

1. Payment stuck in Processing for >15 minutes
2. Payment created with >50k items
3. View/update operation timeout rate >1%
4. Allocation job failure rate >5%

## Rollback Plan

### Phase 0 Rollback

1. Disable feature flag: `payment.guaranteed_transaction_log.enabled=false`
2. Disable feature flag: `payment.scheduler.enhanced_stuck_detection.enabled=false`
3. Revert scheduler to previous version
4. Monitor for 1 hour

### Phase 1 Rollback

1. Disable feature flag: `payment.async_allocation.enabled=false`
2. Disable feature flag: `payment.run.item_validation.enabled=false`
3. Disable feature flag: `payment.paginated_view.enabled=false`
4. Monitor for 1 hour

### Phase 2 Rollback

1. Disable feature flag: `payment.async_update.enabled=false`
2. Revert UI changes
3. Monitor for 1 hour

## Troubleshooting

### Issue: Payments Still Stuck in Processing

**Check:**
1. Is transaction log being created? Check logs
2. Is scheduler running? Check scheduler logs
3. Is timeout configured correctly? Check `application.yml`

**Resolution:**
1. Manually create transaction log using admin tool
2. Restart scheduler
3. Adjust timeout if needed

### Issue: Async Allocation Not Working

**Check:**
1. Is feature flag enabled?
2. Are jobs being queued? Check job queue
3. Are batches processing? Check allocation logs

**Resolution:**
1. Enable feature flag
2. Restart job processor
3. Check for errors in batch processing

### Issue: View Endpoint Timing Out

**Check:**
1. Is pagination enabled?
2. Is summary view being used?
3. Are indexes created?

**Resolution:**
1. Enable pagination feature flag
2. Update UI to use summary view
3. Run index migration

## Support

For issues or questions:
- Slack: #payment-integration-support
- Email: payment-team@zuora.com
- Jira: Create ticket in PAYMENT project
