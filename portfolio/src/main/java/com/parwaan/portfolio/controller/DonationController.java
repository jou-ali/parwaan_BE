package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.dto.DonationOrderRequest;
import com.parwaan.portfolio.dto.DonationVerifyRequest;
import com.parwaan.portfolio.service.DonationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "http://localhost:3000")
public class DonationController {
    private final DonationService donationService;

    public DonationController(DonationService donationService) {
        this.donationService = donationService;
    }

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody DonationOrderRequest request) {
        return ResponseEntity.ok(donationService.createOrder(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@Valid @RequestBody DonationVerifyRequest request) {
        boolean verified = donationService.verifyPayment(request);
        if (!verified) {
            return ResponseEntity.badRequest().body(Map.of("status", "invalid_signature"));
        }
        return ResponseEntity.ok(Map.of("status", "paid"));
    }

    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        donationService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
