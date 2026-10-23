package com.agro_venta.products.infrastructure.persistence.adapter;

import com.agro_venta.products.domain.model.Payment;
import com.agro_venta.products.domain.port.PaymentPort;
import com.agro_venta.products.infrastructure.entity.PaymentEntity;
import com.agro_venta.products.infrastructure.repository.PaymentR2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class PaymentR2dbcAdapter implements PaymentPort {

    private final PaymentR2dbcRepository repository;

    public PaymentR2dbcAdapter(PaymentR2dbcRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Payment> save(Payment payment) {
        return repository.save(toEntity(payment)).map(this::toDomain);
    }

    @Override
    public Mono<Payment> findByProviderTransactionId(String providerTxId) {
        return repository.findByProviderTransactionId(providerTxId).map(this::toDomain);
    }

    @Override
    public Mono<Payment> findByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId).map(this::toDomain);
    }

    private Payment toDomain(PaymentEntity e) {
        return new Payment(
                e.getId(), e.getOrderId(), e.getProvider(),
                e.getProviderTransactionId(), e.getAmount(), e.getCurrency(),
                e.getStatus(), e.getPaymentMethod(), e.getProviderResponse(),
                e.getProcessedAt());
    }

    private PaymentEntity toEntity(Payment p) {
        return PaymentEntity.builder()
                .id(p.id())
                .orderId(p.orderId())
                .provider(p.provider())
                .providerTransactionId(p.providerTransactionId())
                .amount(p.amount())
                .currency(p.currency())
                .status(p.status())
                .paymentMethod(p.paymentMethod())
                .providerResponse(p.providerResponse())
                .processedAt(p.processedAt())
                .build();
    }
}
