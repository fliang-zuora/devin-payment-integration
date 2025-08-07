package com.zuora.cybersource.v2;

public class CyberSourcePaymentResult {
    private String zuoraResponseCode;
    private String gatewayResponseCode;
    private String gatewayResponseMessage;
    private String gatewayReferenceId;
    private String mitReceivedTxId;
    
    public String getZuoraResponseCode() {
        return zuoraResponseCode;
    }
    
    public void setZuoraResponseCode(String zuoraResponseCode) {
        this.zuoraResponseCode = zuoraResponseCode;
    }
    
    public String getGatewayResponseCode() {
        return gatewayResponseCode;
    }
    
    public void setGatewayResponseCode(String gatewayResponseCode) {
        this.gatewayResponseCode = gatewayResponseCode;
    }
    
    public String getGatewayResponseMessage() {
        return gatewayResponseMessage;
    }
    
    public void setGatewayResponseMessage(String gatewayResponseMessage) {
        this.gatewayResponseMessage = gatewayResponseMessage;
    }
    
    public String getGatewayReferenceId() {
        return gatewayReferenceId;
    }
    
    public void setGatewayReferenceId(String gatewayReferenceId) {
        this.gatewayReferenceId = gatewayReferenceId;
    }
    
    public String getMitReceivedTxId() {
        return mitReceivedTxId;
    }
    
    public void setMitReceivedTxId(String mitReceivedTxId) {
        this.mitReceivedTxId = mitReceivedTxId;
    }
}
