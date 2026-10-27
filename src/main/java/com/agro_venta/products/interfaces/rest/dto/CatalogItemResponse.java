package com.agro_venta.products.interfaces.rest.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CatalogItemResponse(
        UUID id,
        String name,
        String description,
        String category,
        BigDecimal quantity,
        String unit,
        BigDecimal unitPrice,
        BigDecimal minShipmentQuantity,
        String originLocation,
        List<String> destinationCities,
        List<String> imageUrls,
        LocalDate harvestDate,
        Instant createdAt
) {}