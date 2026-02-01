package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.DonationOrderRequest;
import com.parwaan.portfolio.dto.DonationVerifyRequest;
import com.parwaan.portfolio.model.Donation;
import com.parwaan.portfolio.repository.DonationRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final DonationRepository donationRepository;

    @Value("${app.razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${app.razorpay.key-secret}")
    private String razorpayKeySecret;

    @Value("${app.razorpay.webhook-secret}")
    private String razorpayWebhookSecret;

    @Value("${app.donations.currency:inr}")
    private String currency;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void initRazorpay() {
        if (razorpayKeyId != null && !razorpayKeyId.isBlank()
                && razorpayKeySecret != null && !razorpayKeySecret.isBlank()) {
            try {
                razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            } catch (Exception e) {
                log.error("Failed to initialize Razorpay client", e);
            }
        }
    }

    @Transactional
    public Map<String, Object> createOrder(DonationOrderRequest request) {
        if (razorpayClient == null) {
            throw new IllegalStateException("Razorpay keys not configured");
        }
        long amountMinor = Math.multiplyExact(request.getAmountRupees(), 100L);

        Donation donation = Donation.builder()
                .amountCents(amountMinor)
                .currency(currency)
                .status("created")
                .donorEmail(request.getEmail())
                .build();
        donationRepository.save(donation);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountMinor);
        orderRequest.put("currency", currency.toUpperCase());
        orderRequest.put("receipt", donation.getId().toString());
        orderRequest.put("payment_capture", 1);

        try {
            Order order = razorpayClient.orders.create(orderRequest);
            donation.setRazorpayOrderId(order.get("id"));
            donationRepository.save(donation);
            return Map.of(
                    "orderId", order.get("id"),
                    "amount", amountMinor,
                    "currency", currency.toUpperCase(),
                    "keyId", razorpayKeyId,
                    "donationId", donation.getId().toString()
            );
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create Razorpay order", e);
        }
    }

    @Transactional
    public boolean verifyPayment(DonationVerifyRequest request) {
        if (razorpayKeySecret == null || razorpayKeySecret.isBlank()) {
            throw new IllegalStateException("Razorpay secret not configured");
        }
        try {
            JSONObject params = new JSONObject();
            params.put("razorpay_order_id", request.getRazorpayOrderId());
            params.put("razorpay_payment_id", request.getRazorpayPaymentId());
            params.put("razorpay_signature", request.getRazorpaySignature());

            boolean verified = Utils.verifyPaymentSignature(params, razorpayKeySecret);
            if (verified) {
                donationRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                        .ifPresentOrElse(donation -> {
                            donation.setStatus("paid");
                            donation.setRazorpayPaymentId(request.getRazorpayPaymentId());
                            donation.setRazorpaySignature(request.getRazorpaySignature());
                            donationRepository.save(donation);
                        }, () -> log.warn("Donation not found for order {}", request.getRazorpayOrderId()));
            }
            return verified;
        } catch (Exception e) {
            throw new IllegalStateException("Razorpay signature verification failed", e);
        }
    }

    @Transactional
    public void handleWebhook(String payload, String signature) {
        if (razorpayWebhookSecret == null || razorpayWebhookSecret.isBlank()) {
            throw new IllegalStateException("Razorpay webhook secret not configured");
        }
        try {
            boolean verified = Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
            if (!verified) {
                throw new IllegalStateException("Invalid Razorpay webhook signature");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Razorpay webhook verification failed", e);
        }

        JSONObject json = new JSONObject(payload);
        String event = json.optString("event");
        if ("payment.captured".equals(event)) {
            JSONObject payment = json.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
            String paymentId = payment.getString("id");
            String orderId = payment.optString("order_id", null);
            var donationOpt = donationRepository.findByRazorpayPaymentId(paymentId);
            if (donationOpt.isEmpty() && orderId != null) {
                donationOpt = donationRepository.findByRazorpayOrderId(orderId);
            }
            donationOpt.ifPresentOrElse(donation -> {
                donation.setStatus("paid");
                donation.setRazorpayPaymentId(paymentId);
                donationRepository.save(donation);
            }, () -> log.warn("Donation not found for payment {}", paymentId));
        } else if ("payment.failed".equals(event)) {
            JSONObject payment = json.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");
            String paymentId = payment.getString("id");
            donationRepository.findByRazorpayPaymentId(paymentId)
                    .ifPresentOrElse(donation -> {
                        donation.setStatus("failed");
                        donationRepository.save(donation);
                    }, () -> log.warn("Donation not found for payment {}", paymentId));
        } else {
            log.debug("Unhandled Razorpay event: {}", event);
        }
    }
}
