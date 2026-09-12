package com.agro_venta.products.interfaces.rest.dto;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record SellRequest(
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(max = 255)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank(message = "La categoría es obligatoria")
        String category,          // FRUTA, VERDURA, GRANO, etc.

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal quantity,

        @NotBlank(message = "La unidad es obligatoria")
        String unit,               // KG, TON, ARROBA, BULTO, CAJA

        @NotNull(message = "El precio unitario es obligatorio")
        @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
        BigDecimal unitPrice,

        @NotBlank(message = "La ubicación de origen es obligatoria")
        String originLocation,

        @Size(max = 5, message = "Máximo 5 imágenes por producto")
        List<MultipartFile> images

) {
}
