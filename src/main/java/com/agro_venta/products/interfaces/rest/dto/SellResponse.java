package com.agro_venta.products.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SellResponse(
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
