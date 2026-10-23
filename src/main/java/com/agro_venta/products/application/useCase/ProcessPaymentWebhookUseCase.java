package com.agro_venta.products.application.useCase;

import com.agro_venta.products.application.dto.WompiWebhookEvent;
import reactor.core.publisher.Mono;

public interface ProcessPaymentWebhookUseCase {
    Mono<Void> process(WompiWebhookEvent event);
}
