package com.parwaan.portfolio.controller;

import com.parwaan.portfolio.model.Donation;
import com.parwaan.portfolio.repository.DonationRepository;

import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/donations")
@CrossOrigin(origins = "http://localhost:3000")
public class DonationController {
    private final DonationRepository repo;
    public DonationController(DonationRepository repo){ this.repo = repo; }

    @PostMapping("/create-session")
    public ResponseEntity<?> createSession(@RequestBody CreateRequest r) {
        Donation d = Donation.builder()
            .amountCents(r.getAmount())
            .currency(r.getCurrency())
            .status("created")
            .donorEmail(r.getEmail())
            .build();
        repo.save(d);
        return ResponseEntity.ok(Map.of("checkoutUrl", "https://example.com/fake-checkout", "sessionId", d.getId()));
    }

    @Getter
    public static class CreateRequest {
        private Long amount;
        private String currency;
        private String email;
    }
}
