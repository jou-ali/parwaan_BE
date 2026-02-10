package com.parwaan.portfolio.dto;

import java.time.Instant;

public class DonationLeaderboardEntry {

    private String email;
    private long amount;
    private String currency;
    private Instant createdAt;

    public DonationLeaderboardEntry() {}

    public DonationLeaderboardEntry(String email, long amount, String currency, Instant createdAt) {
        this.email = email;
        this.amount = amount;
        this.currency = currency;
        this.createdAt = createdAt;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
