package com.agro_venta.products.interfaces.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderRequest(
        @NotNull(message = "El ID del producto es obligatorio")
        UUID productId,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity,

        @NotBlank(message = "La ciudad de destino es obligatoria")
        String destinationCity,

        @NotBlank(message = "La dirección de entrega es obligatoria")
        @Size(max = 500)
        String shippingAddress
) {}
