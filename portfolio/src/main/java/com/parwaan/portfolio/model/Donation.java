package com.parwaan.portfolio.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Donation {
    
    @Id
    @GeneratedValue
    private UUID id;

    private Long amountCents;
    private String currency;
    private String status;
    private String donorEmail;

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private Instant createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = Instant.now();
    }
}
