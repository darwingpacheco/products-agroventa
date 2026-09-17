package com.agro_venta.products.interfaces.rest.dto;

import java.util.UUID;

public record SellResponse(
        UUID id,
        String message,
        String status
) {
}
