package com.agro_venta.products.application.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class SellDTO {
    String name;
    String description;
    String category;
    BigDecimal quantity;
    String unit;
    BigDecimal unitPrice;
    String originLocation;
    List<String> images;
}
