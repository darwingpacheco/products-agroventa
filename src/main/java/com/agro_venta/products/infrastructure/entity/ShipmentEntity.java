package com.agro_venta.products.infrastructure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("shipments")
public class ShipmentEntity {

    @Id
    private UUID id;

    @Column("product_id")
    private UUID productId;

    @Column("total_quantity")
    private BigDecimal totalQuantity;

    @Column("min_quantity_threshold")
    private BigDecimal minQuantityThreshold;

    @Column("status")
    private String status;

    @Column("ready_at")
    private OffsetDateTime readyAt;

    @Column("dispatched_at")
    private OffsetDateTime dispatchedAt;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
