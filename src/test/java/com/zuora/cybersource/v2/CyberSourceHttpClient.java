package com.zuora.cybersource.v2;

import java.util.Map;

public interface CyberSourceHttpClient {
    CyberSourceHttpResponse post(String url, Map<String, String> headers, String body) throws Exception;
}
