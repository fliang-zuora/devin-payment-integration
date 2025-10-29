CREATE INDEX IF NOT EXISTS idx_ar_txn_item_app_source_tenant_deleted 
ON ar_transaction_item_application(source_transaction_id, tenant_id, deleted, id);

CREATE INDEX IF NOT EXISTS idx_ar_txn_item_app_keyset 
ON ar_transaction_item_application(source_transaction_id, id);

CREATE INDEX IF NOT EXISTS idx_payment_status_modified 
ON payment(status, last_modified_date);

CREATE INDEX IF NOT EXISTS idx_transaction_log_payment_id 
ON transaction_log(payment_id);

CREATE INDEX IF NOT EXISTS idx_allocation_job_payment_status 
ON allocation_job(payment_id, status);
