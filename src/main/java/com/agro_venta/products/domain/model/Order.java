package com.agro_venta.products.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Order(
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
) {}
