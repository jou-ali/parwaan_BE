package com.parwaan.portfolio.service;

import com.parwaan.portfolio.dto.DonationCheckoutRequest;
import com.parwaan.portfolio.dto.DonationCheckoutResponse;
import com.parwaan.portfolio.dto.DonationLeaderboardEntry;
import com.parwaan.portfolio.model.Donation;
import com.parwaan.portfolio.repository.DonationRepository;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class DonationService {

    private static final Logger log = LoggerFactory.getLogger(DonationService.class);

    private final DonationRepository donationRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${app.stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${app.stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    @Value("${app.donations.currency:inr}")
    private String defaultCurrency;

    @Value("${app.donations.success-url:http://localhost:8080/donations/success}")
    private String successUrl;

    @Value("${app.donations.cancel-url:http://localhost:8080/donations/failure}")
    private String cancelUrl;

    public DonationService(DonationRepository donationRepository) {
        this.donationRepository = donationRepository;
    }

    @PostConstruct
    public void initStripe() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
        }
    }

    public DonationCheckoutResponse createCheckoutSession(DonationCheckoutRequest request) {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe secret key not configured");
        }

        long amount = request.getAmount();
        String currency = request.getCurrency() == null || request.getCurrency().isBlank()
                ? defaultCurrency
                : request.getCurrency();

        Donation donation = Donation.builder()
                .amount(amount)
                .currency(currency)
                .status("created")
                .donorEmail(request.getEmail())
                .build();
        donationRepository.save(donation);

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount(amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Donation")
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("donationId", donation.getId().toString())
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("donationId", donation.getId().toString())
                                .build()
                );

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            paramsBuilder.setCustomerEmail(request.getEmail());
        }

        SessionCreateParams params = paramsBuilder.build();

        try {
            Session session = Session.create(params);
            donation.setStripeSessionId(session.getId());
            donationRepository.save(donation);
            return new DonationCheckoutResponse(session.getUrl(), session.getId());
        } catch (StripeException e) {
            log.warn("Failed to create Stripe session: {}", e.getMessage());
            throw new IllegalStateException("Failed to create checkout session");
        }
    }

    public void handleWebhook(String payload, String signature) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret not configured");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new IllegalStateException("Invalid Stripe webhook signature");
        }

        if ("checkout.session.completed".equals(event.getType())
                || "checkout.session.async_payment_succeeded".equals(event.getType())) {
            String sessionId = extractObjectId(event);
            if (sessionId == null) {
                log.warn("Stripe session id missing in webhook event");
                return;
            }

            Session session;
            try {
                session = Session.retrieve(sessionId);
            } catch (StripeException e) {
                log.warn("Failed to retrieve Stripe session {}: {}", sessionId, e.getMessage());
                return;
            }

            Donation donation = findOrCreateDonation(session);
            donation.setStatus("paid");
            donation.setStripeSessionId(session.getId());
            donation.setStripePaymentIntentId(session.getPaymentIntent());
            if (session.getCustomerEmail() != null) {
                donation.setDonorEmail(session.getCustomerEmail());
            } else if (session.getCustomerDetails() != null && session.getCustomerDetails().getEmail() != null) {
                donation.setDonorEmail(session.getCustomerDetails().getEmail());
            }
            if (session.getAmountTotal() != null) {
                donation.setAmount(session.getAmountTotal());
            }
            if (session.getCurrency() != null) {
                donation.setCurrency(session.getCurrency());
            }
            donationRepository.save(donation);
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            String paymentIntentId = extractObjectId(event);
            if (paymentIntentId == null) {
                log.warn("Stripe payment intent id missing in webhook event");
                return;
            }
            try {
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
                applyPaymentIntentUpdate(paymentIntent);
            } catch (StripeException e) {
                log.warn("Failed to retrieve payment intent {}: {}", paymentIntentId, e.getMessage());
            }
        }
    }

    public List<DonationLeaderboardEntry> getLeaderboard(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 10));
        return donationRepository.findByStatusOrderByAmountDesc("paid", PageRequest.of(0, safeLimit))
                .map(donation -> new DonationLeaderboardEntry(
                        donation.getDonorEmail(),
                        donation.getAmount(),
                        donation.getCurrency(),
                        donation.getCreatedAt()
                ))
                .getContent();
    }

    private String extractObjectId(Event event) {
        EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();
        try {
            String rawJson = deserializer.getRawJson();
            if (rawJson != null && !rawJson.isBlank()) {
                JsonNode root = mapper.readTree(rawJson);
                String id = root.path("id").asText(null);
                if (id != null && !id.isBlank()) {
                    return id;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse Stripe event object id: {}", e.getMessage());
        }
        return null;
    }

    private Donation findOrCreateDonation(Session session) {
        Map<String, String> metadata = session.getMetadata();
        if (metadata != null && metadata.containsKey("donationId")) {
            String donationId = metadata.get("donationId");
            try {
                Optional<Donation> byId = donationRepository.findById(UUID.fromString(donationId));
                if (byId.isPresent()) {
                    return byId.get();
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid donationId in metadata: {}", donationId);
            }
        }

        return donationRepository.findByStripeSessionId(session.getId())
                .orElseGet(() -> donationRepository.save(Donation.builder()
                        .amount(session.getAmountTotal() == null ? 0L : session.getAmountTotal())
                        .currency(session.getCurrency() == null ? defaultCurrency : session.getCurrency())
                        .status("paid")
                        .stripeSessionId(session.getId())
                        .stripePaymentIntentId(session.getPaymentIntent())
                        .donorEmail(session.getCustomerEmail())
                        .build()));
    }

    private void applyPaymentIntentUpdate(PaymentIntent paymentIntent) {
        Map<String, String> metadata = paymentIntent.getMetadata();
        if (metadata == null || !metadata.containsKey("donationId")) {
            return;
        }

        String donationId = metadata.get("donationId");
        Donation donation;
        try {
            donation = donationRepository.findById(UUID.fromString(donationId)).orElse(null);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid donationId in payment intent metadata: {}", donationId);
            return;
        }
        if (donation == null) {
            return;
        }

        donation.setStatus("paid");
        donation.setStripePaymentIntentId(paymentIntent.getId());
        if (paymentIntent.getAmountReceived() != null) {
            donation.setAmount(paymentIntent.getAmountReceived());
        }
        if (paymentIntent.getCurrency() != null) {
            donation.setCurrency(paymentIntent.getCurrency());
        }
        donationRepository.save(donation);
    }
}
