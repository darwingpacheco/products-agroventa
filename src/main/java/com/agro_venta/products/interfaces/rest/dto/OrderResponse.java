package com.agro_venta.products.interfaces.rest.dto;

import com.agro_venta.products.domain.model.Order;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderResponse(
        UUID           id,
        UUID           productId,
        UUID           buyerId,
        UUID           sellerId,
        BigDecimal     quantity,
        BigDecimal     unitPrice,
        BigDecimal     subtotal,
        BigDecimal     shippingCost,
        BigDecimal     total,
        String         destinationCity,
        String         shippingAddress,
        String         status,
        String         paymentStatus,
        OffsetDateTime reservationExpiresAt,
        OffsetDateTime createdAt
) {
    public static OrderResponse fromDomain(Order order) {
        return new OrderResponse(
                order.id(), order.productId(),
                order.buyerId(), order.sellerId(),
                order.quantity(), order.unitPrice(),
                order.subtotal(), order.shippingCost(), order.total(),
                order.destinationCity(), order.shippingAddress(),
                order.status(), order.paymentStatus(),
                order.reservationExpiresAt(), order.createdAt());
    }
}
