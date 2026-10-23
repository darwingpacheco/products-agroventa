package com.agro_venta.products.interfaces.rest.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SellRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 255)
        String name,

        @Size(max = 2000)
        String description,

        @NotBlank(message = "La categoría es obligatoria")
        String category,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity,

        @NotBlank(message = "La unidad es obligatoria")
        String unit,

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal unitPrice,

        @NotNull(message = "El mínimo de carga para despachar es obligatorio")
        @DecimalMin(value = "0.01", message = "El mínimo de despacho debe ser mayor a 0")
        BigDecimal minShipmentQuantity,

        @NotBlank(message = "La ubicación de origen es obligatoria")
        String originLocation,

        @NotEmpty(message = "Debe indicar al menos una ciudad de destino")
        @Size(max = 6)
        List<String> destinationCities,

        LocalDate harvestDate
) {}
