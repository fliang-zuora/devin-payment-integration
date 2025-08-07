package com.zuora.cybersource.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class CyberSourcePaymentProcessor {
    
    private final CyberSourceHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private Map<String, Object> lastRequest;
    private List<Map<String, Object>> allRequests;
    
    public CyberSourcePaymentProcessor(CyberSourceHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
        this.allRequests = new ArrayList<>();
    }
    
    public CyberSourcePaymentResult processPayment(Map<String, Object> paymentRequest) throws Exception {
        Map<String, Object> requestMap = buildCyberSourceRequest(paymentRequest);
        this.lastRequest = requestMap;
        
        String url = (String) requestMap.get("URL");
        Map<String, String> headers = extractHeaders(requestMap);
        String requestBody = (String) requestMap.get("REQUEST_BODY");
        
        CyberSourceHttpResponse response = httpClient.post(url, headers, requestBody);
        
        return processResponse(response);
    }
    
    public Map<String, Object> getLastRequest() {
        return lastRequest;
    }
    
    public List<Map<String, Object>> getAllRequests() {
        return allRequests;
    }
    
    public CyberSourcePaymentResult processValidation(Map<String, Object> validationRequest) throws Exception {
        String amount = (String) validationRequest.get("Amount");
        boolean isZeroAmount = "0".equals(amount) || "0.00".equals(amount);
        
        if (isZeroAmount) {
            return processZeroAmountValidation(validationRequest);
        } else {
            return processNonZeroAmountValidation(validationRequest);
        }
    }
    
    private CyberSourcePaymentResult processZeroAmountValidation(Map<String, Object> validationRequest) throws Exception {
        Map<String, Object> requestMap = buildZeroAmountValidationRequest(validationRequest);
        this.lastRequest = requestMap;
        this.allRequests.add(requestMap);
        
        String url = (String) requestMap.get("URL");
        Map<String, String> headers = extractHeaders(requestMap);
        String requestBody = (String) requestMap.get("REQUEST_BODY");
        
        CyberSourceHttpResponse response = httpClient.post(url, headers, requestBody);
        
        return processValidationResponse(response, "Zero Amount Validation Approved");
    }
    
    private CyberSourcePaymentResult processNonZeroAmountValidation(Map<String, Object> validationRequest) throws Exception {
        Map<String, Object> authRequestMap = buildNonZeroAmountAuthRequest(validationRequest);
        this.allRequests.add(authRequestMap);
        
        String authUrl = (String) authRequestMap.get("URL");
        Map<String, String> authHeaders = extractHeaders(authRequestMap);
        String authRequestBody = (String) authRequestMap.get("REQUEST_BODY");
        
        CyberSourceHttpResponse authResponse = httpClient.post(authUrl, authHeaders, authRequestBody);
        
        if (authResponse.getStatusCode() != 201) {
            this.lastRequest = authRequestMap;
            return processValidationResponse(authResponse, "Authorization Failed");
        }
        
        JsonNode authResponseJson = objectMapper.readTree(authResponse.getBody());
        String authTransactionId = authResponseJson.path("id").asText();
        
        Map<String, Object> voidRequestMap = buildVoidRequest(validationRequest, authTransactionId);
        this.lastRequest = voidRequestMap;
        this.allRequests.add(voidRequestMap);
        
        String voidUrl = (String) voidRequestMap.get("URL");
        Map<String, String> voidHeaders = extractHeaders(voidRequestMap);
        String voidRequestBody = (String) voidRequestMap.get("REQUEST_BODY");
        
        CyberSourceHttpResponse voidResponse = httpClient.post(voidUrl, voidHeaders, voidRequestBody);
        
        CyberSourcePaymentResult result = processValidationResponse(voidResponse, "Validation Completed - Authorization Voided");
        
        result.setAuthTransactionId(authTransactionId);
        
        return result;
    }
    
    private Map<String, Object> buildZeroAmountValidationRequest(Map<String, Object> validationRequest) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        
        requestMap.put("URL", CyberSourceTestHelper.CYBERSOURCE_TEST_URL);
        requestMap.put("METHOD", "POST");
        requestMap.put("Content-Type", CyberSourceTestHelper.CONTENT_TYPE);
        requestMap.put("Authorization", "Signature test_signature");
        requestMap.put("v-c-merchant-id", validationRequest.get("MerchantId"));
        requestMap.put("v-c-date", "Wed, 15 Jan 2024 10:30:45 GMT");
        requestMap.put("Host", "apitest.cybersource.com");
        
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> clientRef = new HashMap<>();
        clientRef.put("code", "test_validation_" + System.currentTimeMillis());
        requestBody.put("clientReferenceInformation", clientRef);
        
        Map<String, Object> paymentInfo = new HashMap<>();
        Map<String, Object> card = new HashMap<>();
        card.put("number", validationRequest.get("CreditCardNumber"));
        card.put("expirationMonth", validationRequest.get("CreditCardExpirationMonth"));
        card.put("expirationYear", validationRequest.get("CreditCardExpirationYear"));
        card.put("securityCode", validationRequest.get("CreditCardSecurityCode"));
        paymentInfo.put("card", card);
        requestBody.put("paymentInformation", paymentInfo);
        
        Map<String, Object> orderInfo = new HashMap<>();
        Map<String, Object> amountDetails = new HashMap<>();
        amountDetails.put("totalAmount", "0.00");
        amountDetails.put("currency", validationRequest.get("Currency"));
        orderInfo.put("amountDetails", amountDetails);
        requestBody.put("orderInformation", orderInfo);
        
        Map<String, Object> processingInfo = new HashMap<>();
        processingInfo.put("capture", false);
        requestBody.put("processingInformation", processingInfo);
        
        requestMap.put("REQUEST_BODY", objectMapper.writeValueAsString(requestBody));
        
        return requestMap;
    }
    
    private Map<String, Object> buildNonZeroAmountAuthRequest(Map<String, Object> validationRequest) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        
        requestMap.put("URL", CyberSourceTestHelper.CYBERSOURCE_TEST_URL);
        requestMap.put("METHOD", "POST");
        requestMap.put("Content-Type", CyberSourceTestHelper.CONTENT_TYPE);
        requestMap.put("Authorization", "Signature test_signature");
        requestMap.put("v-c-merchant-id", validationRequest.get("MerchantId"));
        requestMap.put("v-c-date", "Wed, 15 Jan 2024 10:30:45 GMT");
        requestMap.put("Host", "apitest.cybersource.com");
        
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> clientRef = new HashMap<>();
        clientRef.put("code", "test_auth_" + System.currentTimeMillis());
        requestBody.put("clientReferenceInformation", clientRef);
        
        Map<String, Object> paymentInfo = new HashMap<>();
        Map<String, Object> card = new HashMap<>();
        card.put("number", validationRequest.get("CreditCardNumber"));
        card.put("expirationMonth", validationRequest.get("CreditCardExpirationMonth"));
        card.put("expirationYear", validationRequest.get("CreditCardExpirationYear"));
        card.put("securityCode", validationRequest.get("CreditCardSecurityCode"));
        paymentInfo.put("card", card);
        requestBody.put("paymentInformation", paymentInfo);
        
        Map<String, Object> orderInfo = new HashMap<>();
        Map<String, Object> amountDetails = new HashMap<>();
        amountDetails.put("totalAmount", validationRequest.get("Amount"));
        amountDetails.put("currency", validationRequest.get("Currency"));
        orderInfo.put("amountDetails", amountDetails);
        requestBody.put("orderInformation", orderInfo);
        
        Map<String, Object> processingInfo = new HashMap<>();
        processingInfo.put("capture", false);
        requestBody.put("processingInformation", processingInfo);
        
        requestMap.put("REQUEST_BODY", objectMapper.writeValueAsString(requestBody));
        
        return requestMap;
    }
    
    private Map<String, Object> buildVoidRequest(Map<String, Object> validationRequest, String authTransactionId) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        
        String voidUrl = CyberSourceTestHelper.CYBERSOURCE_TEST_URL.replace("/payments", "/payments/" + authTransactionId + "/voids");
        requestMap.put("URL", voidUrl);
        requestMap.put("METHOD", "POST");
        requestMap.put("Content-Type", CyberSourceTestHelper.CONTENT_TYPE);
        requestMap.put("Authorization", "Signature test_signature");
        requestMap.put("v-c-merchant-id", validationRequest.get("MerchantId"));
        requestMap.put("v-c-date", "Wed, 15 Jan 2024 10:30:45 GMT");
        requestMap.put("Host", "apitest.cybersource.com");
        
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> clientRef = new HashMap<>();
        clientRef.put("code", "test_void_" + System.currentTimeMillis());
        requestBody.put("clientReferenceInformation", clientRef);
        
        requestMap.put("REQUEST_BODY", objectMapper.writeValueAsString(requestBody));
        
        return requestMap;
    }
    
    private CyberSourcePaymentResult processValidationResponse(CyberSourceHttpResponse response, String successMessage) throws Exception {
        CyberSourcePaymentResult result = new CyberSourcePaymentResult();
        
        result.setGatewayResponseCode(String.valueOf(response.getStatusCode()));
        
        if (response.getStatusCode() == 201) {
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            result.setZuoraResponseCode("Approved");
            result.setGatewayResponseMessage(successMessage);
            result.setGatewayReferenceId(responseJson.path("id").asText());
            result.setMitReceivedTxId(responseJson.path("processorInformation").path("networkTransactionId").asText());
        } else {
            result.setZuoraResponseCode("Unknown");
            result.setGatewayResponseMessage("Error: " + response.getBody());
        }
        
        return result;
    }
    
    private Map<String, Object> buildCyberSourceRequest(Map<String, Object> paymentRequest) throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        
        requestMap.put("URL", CyberSourceTestHelper.CYBERSOURCE_TEST_URL);
        requestMap.put("METHOD", "POST");
        requestMap.put("Content-Type", CyberSourceTestHelper.CONTENT_TYPE);
        requestMap.put("Authorization", "Signature test_signature");
        requestMap.put("v-c-merchant-id", paymentRequest.get("MerchantId"));
        requestMap.put("v-c-date", "Wed, 15 Jan 2024 10:30:45 GMT");
        requestMap.put("Host", "apitest.cybersource.com");
        
        Map<String, Object> requestBody = new HashMap<>();
        
        Map<String, Object> clientRef = new HashMap<>();
        clientRef.put("code", "test_reference_" + System.currentTimeMillis());
        requestBody.put("clientReferenceInformation", clientRef);
        
        Map<String, Object> paymentInfo = new HashMap<>();
        Map<String, Object> card = new HashMap<>();
        card.put("number", paymentRequest.get("CreditCardNumber"));
        card.put("expirationMonth", paymentRequest.get("CreditCardExpirationMonth"));
        card.put("expirationYear", paymentRequest.get("CreditCardExpirationYear"));
        card.put("securityCode", paymentRequest.get("CreditCardSecurityCode"));
        paymentInfo.put("card", card);
        requestBody.put("paymentInformation", paymentInfo);
        
        Map<String, Object> orderInfo = new HashMap<>();
        Map<String, Object> amountDetails = new HashMap<>();
        amountDetails.put("totalAmount", paymentRequest.get("Amount"));
        amountDetails.put("currency", paymentRequest.get("Currency"));
        orderInfo.put("amountDetails", amountDetails);
        requestBody.put("orderInformation", orderInfo);
        
        Map<String, Object> processingInfo = new HashMap<>();
        processingInfo.put("capture", true);
        requestBody.put("processingInformation", processingInfo);
        
        requestMap.put("REQUEST_BODY", objectMapper.writeValueAsString(requestBody));
        
        return requestMap;
    }
    
    private Map<String, String> extractHeaders(Map<String, Object> requestMap) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", (String) requestMap.get("Content-Type"));
        headers.put("Authorization", (String) requestMap.get("Authorization"));
        headers.put("v-c-merchant-id", (String) requestMap.get("v-c-merchant-id"));
        headers.put("v-c-date", (String) requestMap.get("v-c-date"));
        headers.put("Host", (String) requestMap.get("Host"));
        return headers;
    }
    
    private CyberSourcePaymentResult processResponse(CyberSourceHttpResponse response) throws Exception {
        CyberSourcePaymentResult result = new CyberSourcePaymentResult();
        
        result.setGatewayResponseCode(String.valueOf(response.getStatusCode()));
        
        if (response.getStatusCode() == 201) {
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            
            result.setZuoraResponseCode("Approved");
            result.setGatewayResponseMessage("Approved");
            result.setGatewayReferenceId(responseJson.path("id").asText());
            result.setMitReceivedTxId(responseJson.path("processorInformation").path("networkTransactionId").asText());
        } else {
            result.setZuoraResponseCode("Unknown");
            result.setGatewayResponseMessage("Error: " + response.getBody());
        }
        
        return result;
    }
}
