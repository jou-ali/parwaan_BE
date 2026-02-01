package com.parwaan.portfolio.repository;

import com.parwaan.portfolio.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
    Optional<Donation> findByRazorpayOrderId(String orderId);
    Optional<Donation> findByRazorpayPaymentId(String paymentId);
}
