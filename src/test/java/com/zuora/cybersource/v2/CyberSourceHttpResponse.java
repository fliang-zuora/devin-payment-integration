package com.zuora.cybersource.v2;

public class CyberSourceHttpResponse {
    private final int statusCode;
    private final String body;
    
    public CyberSourceHttpResponse(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getBody() {
        return body;
    }
}
