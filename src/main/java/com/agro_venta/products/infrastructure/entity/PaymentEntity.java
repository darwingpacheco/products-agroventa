package com.agro_venta.products.infrastructure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("payments")
public class PaymentEntity {

    @Id
    private UUID id;

    @Column("order_id")
    private UUID orderId;

    @Column("provider")
    private String provider;

    @Column("provider_transaction_id")
    private String providerTransactionId;

    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("status")
    private String status;

    @Column("payment_method")
    private String paymentMethod;

    @Column("provider_response")
    private String providerResponse;   // JSONB stored as String

    @Column("processed_at")
    private OffsetDateTime processedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;
}
