package com.zuora.aitest.payment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RunPaymentRepository {

    private final Map<String, RunPayment> runPayments = new ConcurrentHashMap<>();
    private int idCounter = 1;

    public RunPayment save(RunPayment runPayment) {
        if (runPayment.getId() == null) {
            runPayment.setId(String.valueOf(idCounter++));
        }
        runPayments.put(runPayment.getId(), runPayment);
        return runPayment;
    }

    public RunPayment findById(String id) {
        return runPayments.get(id);
    }

    public List<RunPayment> findAll() {
        return new ArrayList<>(runPayments.values());
    }

    public List<RunPayment> findByPaymentRunId(String paymentRunId) {
        return runPayments.values().stream()
                .filter(rp -> rp.getPaymentRunId().equals(paymentRunId))
                .collect(Collectors.toList());
    }

    public RunPayment findByPaymentRunIdAndInvoiceId(String paymentRunId, String invoiceId) {
        return runPayments.values().stream()
                .filter(rp -> rp.getPaymentRunId().equals(paymentRunId) 
                        && rp.getInvoiceId().equals(invoiceId))
                .findFirst()
                .orElse(null);
    }

    public boolean isInvoiceProcessedInRun(String paymentRunId, String invoiceId) {
        return findByPaymentRunIdAndInvoiceId(paymentRunId, invoiceId) != null;
    }

    public void deleteAll() {
        runPayments.clear();
        idCounter = 1;
    }
}
