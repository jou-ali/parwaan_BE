package com.parwaan.portfolio.dto;

public class DonationCheckoutResponse {

    private String checkoutUrl;
    private String sessionId;

    public DonationCheckoutResponse() {}

    public DonationCheckoutResponse(String checkoutUrl, String sessionId) {
        this.checkoutUrl = checkoutUrl;
        this.sessionId = sessionId;
    }

    public String getCheckoutUrl() { return checkoutUrl; }
    public void setCheckoutUrl(String checkoutUrl) { this.checkoutUrl = checkoutUrl; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
