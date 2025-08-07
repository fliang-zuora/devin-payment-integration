package com.zuora.cybersource.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CyberSourceV2CreditCardValidateUnitTest {

    private ObjectMapper objectMapper;
    private CyberSourcePaymentProcessor paymentProcessor;

    @Mock
    private CyberSourceHttpClient httpClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        paymentProcessor = new CyberSourcePaymentProcessor(httpClient);
    }

    @Test
    void case_01_ItShouldSucceedForZeroAmountValidation() throws Exception {
        String mockResponse = loadJsonFromFile("src/test/resources/com/zuora/opg/test/json/cybersource_2/creditcard/validate/case_01/zero_amount_validation_response.json");
        
        when(httpClient.post(any(String.class), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(201, mockResponse));

        Map<String, Object> validationRequest = createZeroAmountValidationRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processValidation(validationRequest);

        assertEquals("Approved", result.getZuoraResponseCode());
        assertEquals("201", result.getGatewayResponseCode());
        assertEquals("Zero Amount Validation Approved", result.getGatewayResponseMessage());
        assertEquals("6853441676996176204955", result.getGatewayReferenceId());
        assertEquals("016150703802095", result.getMitReceivedTxId());

        verify(httpClient, times(1)).post(any(String.class), any(Map.class), any(String.class));

        Map<String, Object> actualRequest = paymentProcessor.getLastRequest();
        
        assertEquals(CyberSourceTestHelper.CYBERSOURCE_TEST_URL, actualRequest.get("URL"));
        assertEquals("POST", actualRequest.get("METHOD"));
        assertEquals(CyberSourceTestHelper.CONTENT_TYPE, actualRequest.get("Content-Type"));

        assertNotNull(actualRequest.get("Authorization"));
        assertNotNull(actualRequest.get("v-c-merchant-id"));
        assertNotNull(actualRequest.get("v-c-date"));
        assertNotNull(actualRequest.get("Host"));

        String requestBody = (String) actualRequest.get("REQUEST_BODY");
        JsonNode requestJson = objectMapper.readTree(requestBody);
        
        assertEquals("4111111111111111", requestJson.path("paymentInformation").path("card").path("number").asText());
        assertEquals("0.00", requestJson.path("orderInformation").path("amountDetails").path("totalAmount").asText());
        assertEquals("USD", requestJson.path("orderInformation").path("amountDetails").path("currency").asText());
        assertFalse(requestJson.path("processingInformation").path("capture").asBoolean());
        assertNotNull(requestJson.path("clientReferenceInformation").path("code").asText());
    }

    @Test
    void case_02_ItShouldSucceedForNonZeroAmountValidation() throws Exception {
        String authResponse = loadJsonFromFile("src/test/resources/com/zuora/opg/test/json/cybersource_2/creditcard/validate/case_02/auth_response.json");
        String voidResponse = loadJsonFromFile("src/test/resources/com/zuora/opg/test/json/cybersource_2/creditcard/validate/case_02/void_response.json");
        
        when(httpClient.post(eq(CyberSourceTestHelper.CYBERSOURCE_TEST_URL), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(201, authResponse));
        
        when(httpClient.post(eq("https://apitest.cybersource.com/pts/v2/payments/6853441676996176204956/voids"), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(201, voidResponse));

        Map<String, Object> validationRequest = createNonZeroAmountValidationRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processValidation(validationRequest);

        assertEquals("Approved", result.getZuoraResponseCode());
        assertEquals("201", result.getGatewayResponseCode());
        assertEquals("Validation Completed - Authorization Voided", result.getGatewayResponseMessage());
        assertEquals("6853441676996176204957", result.getGatewayReferenceId());
        assertEquals("6853441676996176204956", result.getAuthTransactionId());

        verify(httpClient, times(2)).post(any(String.class), any(Map.class), any(String.class));

        List<Map<String, Object>> allRequests = paymentProcessor.getAllRequests();
        assertEquals(2, allRequests.size());

        Map<String, Object> authRequest = allRequests.get(0);
        assertEquals(CyberSourceTestHelper.CYBERSOURCE_TEST_URL, authRequest.get("URL"));
        String authRequestBody = (String) authRequest.get("REQUEST_BODY");
        JsonNode authRequestJson = objectMapper.readTree(authRequestBody);
        assertEquals("100.00", authRequestJson.path("orderInformation").path("amountDetails").path("totalAmount").asText());
        assertFalse(authRequestJson.path("processingInformation").path("capture").asBoolean());

        Map<String, Object> voidRequest = paymentProcessor.getLastRequest();
        assertTrue(((String) voidRequest.get("URL")).contains("/voids"));
        String voidRequestBody = (String) voidRequest.get("REQUEST_BODY");
        JsonNode voidRequestJson = objectMapper.readTree(voidRequestBody);
        assertNotNull(voidRequestJson.path("clientReferenceInformation").path("code").asText());
    }

    @Test
    void case_03_ReturnUnknownZuoraResponseCodeWhenZeroAmountValidationFails() throws Exception {
        when(httpClient.post(any(String.class), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(400, "{\"message\":\"Invalid card number\"}"));

        Map<String, Object> validationRequest = createZeroAmountValidationRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processValidation(validationRequest);

        assertEquals("Unknown", result.getZuoraResponseCode());
        assertEquals("400", result.getGatewayResponseCode());
        assertTrue(result.getGatewayResponseMessage().contains("Error"));
    }

    @Test
    void case_04_ReturnUnknownZuoraResponseCodeWhenNonZeroAmountAuthFails() throws Exception {
        when(httpClient.post(any(String.class), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(402, "{\"message\":\"Insufficient funds\"}"));

        Map<String, Object> validationRequest = createNonZeroAmountValidationRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processValidation(validationRequest);

        assertEquals("Unknown", result.getZuoraResponseCode());
        assertEquals("402", result.getGatewayResponseCode());
        assertTrue(result.getGatewayResponseMessage().contains("Error"));

        verify(httpClient, times(1)).post(any(String.class), any(Map.class), any(String.class));
    }

    @Test
    void case_05_ReturnUnknownZuoraResponseCodeWhenVoidFails() throws Exception {
        String authResponse = loadJsonFromFile("src/test/resources/com/zuora/opg/test/json/cybersource_2/creditcard/validate/case_02/auth_response.json");
        
        when(httpClient.post(eq(CyberSourceTestHelper.CYBERSOURCE_TEST_URL), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(201, authResponse));
        
        when(httpClient.post(eq("https://apitest.cybersource.com/pts/v2/payments/6853441676996176204956/voids"), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(500, "{\"message\":\"Void failed\"}"));

        Map<String, Object> validationRequest = createNonZeroAmountValidationRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processValidation(validationRequest);

        assertEquals("Unknown", result.getZuoraResponseCode());
        assertEquals("500", result.getGatewayResponseCode());
        assertTrue(result.getGatewayResponseMessage().contains("Error"));
        assertEquals("6853441676996176204956", result.getAuthTransactionId());

        verify(httpClient, times(2)).post(any(String.class), any(Map.class), any(String.class));
    }

    @Test
    void testValidationRequestPayloadExtraction() throws Exception {
        Map<String, Object> zeroAmountRequestMap = createMockZeroAmountValidationRequestMap();
        Map<String, Object> nonZeroAmountRequestMap = createMockNonZeroAmountValidationRequestMap();
        
        assertEquals(CyberSourceTestHelper.CYBERSOURCE_TEST_URL, CyberSourceRequestPayloadExtractor.extractUrl(zeroAmountRequestMap));
        assertEquals("POST", CyberSourceRequestPayloadExtractor.extractMethod(zeroAmountRequestMap));
        assertEquals(CyberSourceTestHelper.CONTENT_TYPE, CyberSourceRequestPayloadExtractor.extractContentType(zeroAmountRequestMap));
        assertEquals("4111111111111111", CyberSourceRequestPayloadExtractor.extractCardNumber(zeroAmountRequestMap));
        assertEquals("0.00", CyberSourceRequestPayloadExtractor.extractTotalAmount(zeroAmountRequestMap));
        assertEquals("USD", CyberSourceRequestPayloadExtractor.extractCurrency(zeroAmountRequestMap));
        assertFalse(CyberSourceRequestPayloadExtractor.extractCaptureFlag(zeroAmountRequestMap));

        assertEquals("100.00", CyberSourceRequestPayloadExtractor.extractTotalAmount(nonZeroAmountRequestMap));
        assertFalse(CyberSourceRequestPayloadExtractor.extractCaptureFlag(nonZeroAmountRequestMap));
    }

    private Map<String, Object> createZeroAmountValidationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("Amount", "0.00");
        request.put("Currency", "USD");
        request.put("CreditCardNumber", "4111111111111111");
        request.put("CreditCardExpirationMonth", "12");
        request.put("CreditCardExpirationYear", "2025");
        request.put("CreditCardHolderName", "Test Cardholder");
        request.put("CreditCardSecurityCode", "123");
        request.put("MerchantId", "test_merchant_id");
        request.put("IsTest", "true");
        return request;
    }

    private Map<String, Object> createNonZeroAmountValidationRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("Amount", "100.00");
        request.put("Currency", "USD");
        request.put("CreditCardNumber", "4111111111111111");
        request.put("CreditCardExpirationMonth", "12");
        request.put("CreditCardExpirationYear", "2025");
        request.put("CreditCardHolderName", "Test Cardholder");
        request.put("CreditCardSecurityCode", "123");
        request.put("MerchantId", "test_merchant_id");
        request.put("IsTest", "true");
        return request;
    }

    private Map<String, Object> createMockZeroAmountValidationRequestMap() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("URL", CyberSourceTestHelper.CYBERSOURCE_TEST_URL);
        requestMap.put("METHOD", "POST");
        requestMap.put("Content-Type", CyberSourceTestHelper.CONTENT_TYPE);
        requestMap.put("Authorization", "Signature test_signature");
        requestMap.put("v-c-merchant-id", "test_merchant_id");
        requestMap.put("v-c-date", "Wed, 15 Jan 2024 10:30:45 GMT");
        requestMap.put("Host", "apitest.cybersource.com");
        
        String requestBody = "{\n" +
                "  \"clientReferenceInformation\": {\n" +
                "    \"code\": \"test_validation_123\"\n" +
                "  },\n" +
                "  \"paymentInformation\": {\n" +
                "    \"card\": {\n" +
                "      \"number\": \"4111111111111111\",\n" +
                "      \"expirationMonth\": \"12\",\n" +
                "      \"expirationYear\": \"2025\",\n" +
                "      \"securityCode\": \"123\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"orderInformation\": {\n" +
                "    \"amountDetails\": {\n" +
                "      \"totalAmount\": \"0.00\",\n" +
                "      \"currency\": \"USD\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"processingInformation\": {\n" +
                "    \"capture\": false\n" +
                "  }\n" +
                "}";
        
        requestMap.put("REQUEST_BODY", requestBody);
        return requestMap;
    }

    private Map<String, Object> createMockNonZeroAmountValidationRequestMap() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("URL", CyberSourceTestHelper.CYBERSOURCE_TEST_URL);
        requestMap.put("METHOD", "POST");
        requestMap.put("Content-Type", CyberSourceTestHelper.CONTENT_TYPE);
        requestMap.put("Authorization", "Signature test_signature");
        requestMap.put("v-c-merchant-id", "test_merchant_id");
        requestMap.put("v-c-date", "Wed, 15 Jan 2024 10:30:45 GMT");
        requestMap.put("Host", "apitest.cybersource.com");
        
        String requestBody = "{\n" +
                "  \"clientReferenceInformation\": {\n" +
                "    \"code\": \"test_auth_123\"\n" +
                "  },\n" +
                "  \"paymentInformation\": {\n" +
                "    \"card\": {\n" +
                "      \"number\": \"4111111111111111\",\n" +
                "      \"expirationMonth\": \"12\",\n" +
                "      \"expirationYear\": \"2025\",\n" +
                "      \"securityCode\": \"123\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"orderInformation\": {\n" +
                "    \"amountDetails\": {\n" +
                "      \"totalAmount\": \"100.00\",\n" +
                "      \"currency\": \"USD\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"processingInformation\": {\n" +
                "    \"capture\": false\n" +
                "  }\n" +
                "}";
        
        requestMap.put("REQUEST_BODY", requestBody);
        return requestMap;
    }

    private String loadJsonFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
