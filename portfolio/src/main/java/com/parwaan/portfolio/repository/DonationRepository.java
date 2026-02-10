package com.parwaan.portfolio.repository;

import com.parwaan.portfolio.model.Donation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DonationRepository extends JpaRepository<Donation, UUID> {
    Optional<Donation> findByStripeSessionId(String stripeSessionId);
    Page<Donation> findByStatusOrderByAmountDesc(String status, Pageable pageable);
}
