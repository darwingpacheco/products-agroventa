package com.agro_venta.products.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record Product(
        UUID           id,
        UUID           sellerId,
        String         name,
        String         description,
        String         category,
        BigDecimal     quantity,
        BigDecimal     reservedQuantity,
        String         unit,
        BigDecimal     unitPrice,
        BigDecimal     totalPrice,
        BigDecimal     minShipmentQuantity,
        String         originLocation,
        List<String>   destinationCities,
        List<String>   imageUrls,
        String         status,
        LocalDate      harvestDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
