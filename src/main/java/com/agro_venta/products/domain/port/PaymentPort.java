package com.agro_venta.products.domain.port;

import com.agro_venta.products.domain.model.Payment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentPort {

    /** Persists a payment record. */
    Mono<Payment> save(Payment payment);

    /** Find by Wompi provider transaction id — for idempotency. */
    Mono<Payment> findByProviderTransactionId(String providerTxId);

    /** Find by order id. */
    Mono<Payment> findByOrderId(UUID orderId);
}
