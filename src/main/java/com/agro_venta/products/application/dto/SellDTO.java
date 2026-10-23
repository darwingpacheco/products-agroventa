package com.agro_venta.products.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SellDTO {
    UUID         sellerId;            // extracted from JWT, never from request body
    String       name;
    String       description;
    String       category;
    BigDecimal   quantity;
    String       unit;
    BigDecimal   unitPrice;
    BigDecimal   minShipmentQuantity;
    String       originLocation;
    List<String> destinationCities;
    List<String> images;
    LocalDate    harvestDate;
}
