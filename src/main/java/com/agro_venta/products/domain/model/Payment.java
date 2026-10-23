package com.agro_venta.products.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record Payment(
        UUID           id,
        UUID           orderId,
        String         provider,
        String         providerTransactionId,
        BigDecimal     amount,
        String         currency,
        String         status,
        String         paymentMethod,
        String         providerResponse,
        OffsetDateTime processedAt
) {}
