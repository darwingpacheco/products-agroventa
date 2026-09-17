package com.agro_venta.products.domain.model;

import java.util.UUID;

public record SoldProduct(
        UUID id,
        String message,
        String status
) {
}
