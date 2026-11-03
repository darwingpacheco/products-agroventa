package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.application.dto.WompiWebhookEvent;
import com.agro_venta.products.application.useCase.ProcessPaymentWebhookUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Handles Wompi payment webhooks.
 *
 * Wompi sandbox (development): https://sandbox.wompi.co
 * Register the webhook URL in the Wompi dashboard under "Eventos".
 * No API key is needed for the webhook itself; signature validation
 * is done inside ProcessPaymentWebhookUseCase.
 */
@RestController
@RequestMapping("/api/agro-venta/v1/payments")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final ProcessPaymentWebhookUseCase webhookUseCase;

    public PaymentController(ProcessPaymentWebhookUseCase webhookUseCase) {
        this.webhookUseCase = webhookUseCase;
    }

    /**
     * Wompi calls this endpoint when a transaction status changes.
     * URL must be registered in the Wompi sandbox dashboard.
     * This endpoint is public (no JWT required) — Wompi doesn't send a Bearer token.
     */
    @PostMapping("/webhook")
    public Mono<ResponseEntity<Void>> handleWompiWebhook(
            @RequestBody WompiWebhookEvent event) {
        log.info("Received Wompi webhook: event={} txId={}",
                event.event(),
                event.data() != null && event.data().transaction() != null
                        ? event.data().transaction().id() : "unknown");

        return webhookUseCase.process(event)
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error processing Wompi webhook", e);
                    // Always return 200 to Wompi so they don't retry indefinitely
                    return Mono.just(ResponseEntity.ok().<Void>build());
                });
    }

    /**
     * App-side payment confirmation. Called by the Android app after the
     * Wompi checkout redirect URL shows status=APPROVED. This is a fallback
     * for when the Wompi webhook is not configured or hasn't arrived yet.
     * Requires JWT — the caller must be the buyer of the order.
     */
    @PostMapping("/confirm/{orderId}")
    public Mono<ResponseEntity<Void>> confirmPayment(
            @PathVariable String orderId,
            @org.springframework.security.core.annotation.AuthenticationPrincipal
                    String userId) {
        log.info("App-side payment confirmation for order {} by user {}", orderId, userId);
        return webhookUseCase.confirmPayment(
                        java.util.UUID.fromString(orderId),
                        java.util.UUID.fromString(userId))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorResume(e -> {
                    log.error("Error confirming payment for order {}", orderId, e);
                    return Mono.just(ResponseEntity.status(500).<Void>build());
                });
    }
}
