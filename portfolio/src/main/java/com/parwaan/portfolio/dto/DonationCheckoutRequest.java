package com.parwaan.portfolio.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

public class DonationCheckoutRequest {

    @NotNull
    @Min(1)
    private Long amount;

    private String currency;

    @NotBlank
    @Email
    private String email;

    public DonationCheckoutRequest() {}

    public DonationCheckoutRequest(Long amount, String currency, String email) {
        this.amount = amount;
        this.currency = currency;
        this.email = email;
    }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
