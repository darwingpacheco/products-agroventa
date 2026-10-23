package com.agro_venta.products.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Shipment(
        UUID           id,
        UUID           productId,
        BigDecimal     totalQuantity,
        BigDecimal     minQuantityThreshold,
        String         status,
        OffsetDateTime readyAt,
        OffsetDateTime dispatchedAt
) {}
