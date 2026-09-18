package com.agro_venta.products.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SoldProduct(
        UUID id,
        UUID sellerId,
        String name,
        String description,
        String category,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        String originLocation,
        List<String> imageUrls,
        String message,
        String status
) {
}
