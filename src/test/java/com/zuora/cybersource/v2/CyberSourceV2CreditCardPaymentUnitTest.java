package com.zuora.cybersource.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

public class CyberSourceV2CreditCardPaymentUnitTest {

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
    void case_01_ItShouldSucceedForNormalRecurringPayment() throws Exception {
        String mockResponse = loadJsonFromFile("src/test/resources/com/zuora/opg/test/json/cybersource_2/creditcard/payment/case_01/payment_response.json");
        
        when(httpClient.post(any(String.class), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(201, mockResponse));

        Map<String, Object> paymentRequest = createTestPaymentRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processPayment(paymentRequest);

        assertEquals("Approved", result.getZuoraResponseCode());
        assertEquals("201", result.getGatewayResponseCode());
        assertEquals("Approved", result.getGatewayResponseMessage());
        assertEquals("6853441676996176204954", result.getGatewayReferenceId());
        assertEquals("016150703802094", result.getMitReceivedTxId());

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
        assertEquals("100.00", requestJson.path("orderInformation").path("amountDetails").path("totalAmount").asText());
        assertEquals("USD", requestJson.path("orderInformation").path("amountDetails").path("currency").asText());
        assertTrue(requestJson.path("processingInformation").path("capture").asBoolean());
        assertNotNull(requestJson.path("clientReferenceInformation").path("code").asText());
    }

    @Test
    void case_02_ReturnUnknownZuoraResponseCodeWhenGatewayReturns5XXHttpStatus() throws Exception {
        when(httpClient.post(any(String.class), any(Map.class), any(String.class)))
                .thenReturn(new CyberSourceHttpResponse(500, "{\"message\":\"Internal Server Error\"}"));

        Map<String, Object> paymentRequest = createTestPaymentRequest();
        
        CyberSourcePaymentResult result = paymentProcessor.processPayment(paymentRequest);

        assertEquals("Unknown", result.getZuoraResponseCode());
        assertEquals("500", result.getGatewayResponseCode());
        assertTrue(result.getGatewayResponseMessage().contains("Error"));
    }

    @Test
    void testCyberSourceRequestPayloadExtraction() throws Exception {
        Map<String, Object> requestMap = createMockRequestMap();
        
        assertEquals(CyberSourceTestHelper.CYBERSOURCE_TEST_URL, CyberSourceRequestPayloadExtractor.extractUrl(requestMap));
        assertEquals("POST", CyberSourceRequestPayloadExtractor.extractMethod(requestMap));
        assertEquals(CyberSourceTestHelper.CONTENT_TYPE, CyberSourceRequestPayloadExtractor.extractContentType(requestMap));
        assertEquals("4111111111111111", CyberSourceRequestPayloadExtractor.extractCardNumber(requestMap));
        assertEquals("100.00", CyberSourceRequestPayloadExtractor.extractTotalAmount(requestMap));
        assertEquals("USD", CyberSourceRequestPayloadExtractor.extractCurrency(requestMap));
        assertTrue(CyberSourceRequestPayloadExtractor.extractCaptureFlag(requestMap));
    }

    private Map<String, Object> createTestPaymentRequest() {
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

    private Map<String, Object> createMockRequestMap() {
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
                "    \"code\": \"test_reference_123\"\n" +
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
                "    \"capture\": true\n" +
                "  }\n" +
                "}";
        
        requestMap.put("REQUEST_BODY", requestBody);
        return requestMap;
    }

    private String loadJsonFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
}
