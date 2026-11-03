package com.agro_venta.products.application.useCase;

import com.agro_venta.products.application.dto.WompiWebhookEvent;
import reactor.core.publisher.Mono;

public interface ProcessPaymentWebhookUseCase {
    Mono<Void> process(WompiWebhookEvent event);

    /**
     * App-side payment confirmation. Used as a fallback when the Wompi
     * webhook is not configured or hasn't arrived yet. The caller must be
     * the buyer of the order.
     */
    Mono<Void> confirmPayment(java.util.UUID orderId, java.util.UUID buyerId);
}
