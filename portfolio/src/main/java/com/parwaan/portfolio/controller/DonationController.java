package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.dto.DonationCheckoutRequest;
import com.parwaan.portfolio.dto.DonationCheckoutResponse;
import com.parwaan.portfolio.dto.DonationLeaderboardEntry;
import com.parwaan.portfolio.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
public class DonationController {

    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<DonationCheckoutResponse> createCheckout(@Valid @RequestBody DonationCheckoutRequest request) {
        return ResponseEntity.ok(donationService.createCheckoutSession(request));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String payload,
                                        @RequestHeader("Stripe-Signature") String signature) {
        donationService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<DonationLeaderboardEntry>> leaderboard(@RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok(donationService.getLeaderboard(limit));
    }
}
