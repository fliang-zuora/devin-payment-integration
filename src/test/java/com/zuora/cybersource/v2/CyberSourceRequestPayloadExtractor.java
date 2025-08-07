package com.zuora.cybersource.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class CyberSourceRequestPayloadExtractor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String extractRequestBody(Map<String, Object> requestMap) {
        return (String) requestMap.get("REQUEST_BODY");
    }

    public static JsonNode parseRequestBody(Map<String, Object> requestMap) throws Exception {
        String requestBody = extractRequestBody(requestMap);
        return objectMapper.readTree(requestBody);
    }

    public static String extractClientReferenceCode(Map<String, Object> requestMap) throws Exception {
        JsonNode jsonNode = parseRequestBody(requestMap);
        return jsonNode.path("clientReferenceInformation").path("code").asText();
    }

    public static String extractCardNumber(Map<String, Object> requestMap) throws Exception {
        JsonNode jsonNode = parseRequestBody(requestMap);
        return jsonNode.path("paymentInformation").path("card").path("number").asText();
    }

    public static String extractTotalAmount(Map<String, Object> requestMap) throws Exception {
        JsonNode jsonNode = parseRequestBody(requestMap);
        return jsonNode.path("orderInformation").path("amountDetails").path("totalAmount").asText();
    }

    public static String extractCurrency(Map<String, Object> requestMap) throws Exception {
        JsonNode jsonNode = parseRequestBody(requestMap);
        return jsonNode.path("orderInformation").path("amountDetails").path("currency").asText();
    }

    public static boolean extractCaptureFlag(Map<String, Object> requestMap) throws Exception {
        JsonNode jsonNode = parseRequestBody(requestMap);
        return jsonNode.path("processingInformation").path("capture").asBoolean();
    }

    public static String extractUrl(Map<String, Object> requestMap) {
        return (String) requestMap.get("URL");
    }

    public static String extractMethod(Map<String, Object> requestMap) {
        return (String) requestMap.get("METHOD");
    }

    public static String extractContentType(Map<String, Object> requestMap) {
        return (String) requestMap.get("Content-Type");
    }

    public static String extractAuthorization(Map<String, Object> requestMap) {
        return (String) requestMap.get("Authorization");
    }

    public static String extractMerchantId(Map<String, Object> requestMap) {
        return (String) requestMap.get("v-c-merchant-id");
    }

    public static String extractDate(Map<String, Object> requestMap) {
        return (String) requestMap.get("v-c-date");
    }

    public static String extractHost(Map<String, Object> requestMap) {
        return (String) requestMap.get("Host");
    }
}
