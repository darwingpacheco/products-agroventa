package com.agro_venta.products.infrastructure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Table("orders")
public class OrderEntity {

    @Id
    private UUID id;

    @Column("product_id")
    private UUID productId;

    @Column("buyer_id")
    private UUID buyerId;

    @Column("seller_id")
    private UUID sellerId;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @Column("subtotal")
    private BigDecimal subtotal;

    @Column("shipping_cost")
    private BigDecimal shippingCost;

    @Column("total")
    private BigDecimal total;

    @Column("destination_city")
    private String destinationCity;

    @Column("shipping_address")
    private String shippingAddress;

    @Column("status")
    private String status;

    @Column("payment_status")
    private String paymentStatus;

    @Column("reservation_expires_at")
    private OffsetDateTime reservationExpiresAt;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
