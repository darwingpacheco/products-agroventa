package com.agro_venta.products.infrastructure.repository;

import com.agro_venta.products.infrastructure.entity.PaymentEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentR2dbcRepository extends ReactiveCrudRepository<PaymentEntity, UUID> {

    Mono<PaymentEntity> findByProviderTransactionId(String providerTransactionId);

    Mono<PaymentEntity> findByOrderId(UUID orderId);
}
