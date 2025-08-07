package com.zuora.cybersource.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

public class CyberSourcePaymentProcessor {
    
    private final CyberSourceHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private Map<String, Object> lastRequest;
    
    public CyberSourcePaymentProcessor(CyberSourceHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
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
