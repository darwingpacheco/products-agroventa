package com.agro_venta.products.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderCommand(
        UUID       productId,
        UUID       buyerId,
        BigDecimal quantity,
        String     destinationCity,
        String     shippingAddress
) {}
