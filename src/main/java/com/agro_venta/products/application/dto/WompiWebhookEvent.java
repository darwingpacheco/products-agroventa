package com.agro_venta.products.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Wompi webhook payload (simplified).
 * Wompi sends POST to our webhook endpoint when a transaction changes state.
 *
 * @see <a href="https://docs.wompi.co/docs/en/webhooks">Wompi Webhooks Docs</a>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record WompiWebhookEvent(
        String event,
        @JsonProperty("data") TransactionData data,
        String timestamp,
        String signature
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionData(
            @JsonProperty("transaction") Transaction transaction
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Transaction(
            String id,
            String status,
            @JsonProperty("amount_in_cents") Long amountInCents,
            String currency,
            @JsonProperty("payment_method_type") String paymentMethodType,
            String reference   // our orderId
    ) {}
}
