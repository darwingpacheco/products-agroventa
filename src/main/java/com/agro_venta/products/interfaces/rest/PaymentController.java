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
                .then(Mono.just(ResponseEntity.<Void>ok().build()))
                .onErrorResume(e -> {
                    log.error("Error processing Wompi webhook", e);
                    // Always return 200 to Wompi so they don't retry indefinitely
                    return Mono.just(ResponseEntity.<Void>ok().build());
                });
    }
}
