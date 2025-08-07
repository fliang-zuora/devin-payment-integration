package com.zuora.cybersource.v2;

import java.util.HashMap;
import java.util.Map;

public class CyberSourceTestHelper {

    public static Map<String, Object> buildGatewayInstanceForTest() {
        Map<String, Object> paymentGateway = new HashMap<>();
        paymentGateway.put("gatewayName", "CyberSource_2");
        paymentGateway.put("gatewayType", "CyberSource");
        paymentGateway.put("isTest", true);
        return paymentGateway;
    }

    public static Map<String, Object> buildGoodCreditCardForTest() {
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("creditCardNumber", "4111111111111111");
        paymentMethod.put("creditCardExpirationMonth", 12);
        paymentMethod.put("creditCardExpirationYear", 2025);
        paymentMethod.put("creditCardHolderName", "Test Cardholder");
        paymentMethod.put("creditCardType", "Visa");
        paymentMethod.put("creditCardSecurityCode", "123");
        return paymentMethod;
    }

    public static Map<String, Object> buildPaymentGatewaySettingRepositoryForTest(String merchantId) {
        Map<String, Object> repository = new HashMap<>();
        if (merchantId != null) {
            repository.put("MerchantId", merchantId);
        } else {
            repository.put("MerchantId", "test_merchant_id");
        }
        repository.put("SharedSecret", "test_shared_secret");
        repository.put("KeyId", "test_key_id");
        repository.put("IsTest", "true");
        return repository;
    }

    public static final String CYBERSOURCE_TEST_URL = "https://apitest.cybersource.com/pts/v2/payments";
    public static final String CYBERSOURCE_PROD_URL = "https://api.cybersource.com/pts/v2/payments";
    public static final String CONTENT_TYPE = "application/json";
    public static final int SUCCESS_STATUS_CODE = 201;
    public static final String SUCCESS_RESPONSE_MESSAGE = "Approved";
}
