package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.application.useCase.SellUseCase;
import com.agro_venta.products.infrastructure.mapper.SellProductMapper;
import com.agro_venta.products.infrastructure.service.S3ImageStorageService;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agro-venta/v1/products")
public class ProductController {

    private final SellUseCase sellUseCase;
    private final S3ImageStorageService s3ImageStorageService;

    public ProductController(SellUseCase sellUseCase, S3ImageStorageService s3ImageStorageService) {
        this.sellUseCase = sellUseCase;
        this.s3ImageStorageService = s3ImageStorageService;
    }

    @PostMapping(value = "/sell", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<SellResponse>> sellProduct(
            ServerWebExchange exchange,
            @AuthenticationPrincipal String userId) {

        return exchange.getMultipartData().flatMap(multipartData -> {
            // 1. Extraer archivos de imagen
            List<Part> imageParts = multipartData.get("images");
            Flux<FilePart> filePartFlux = Flux.empty();
            if (imageParts != null) {
                filePartFlux = Flux.fromIterable(imageParts)
                        .filter(part -> part instanceof FilePart)
                        .cast(FilePart.class);
            }

            // 2. Extraer campos de texto enviados por Retrofit @Part
            String name = getFormFieldValue(multipartData, "name");
            String description = getFormFieldValue(multipartData, "description");
            String category = getFormFieldValue(multipartData, "category");
            String unit = getFormFieldValue(multipartData, "unit");
            String originLocation = getFormFieldValue(multipartData, "originLocation");
            String destinationCitiesRaw = getFormFieldValue(multipartData, "destinationCities");

            BigDecimal quantity = parseBigDecimal(getFormFieldValue(multipartData, "quantity"));
            BigDecimal unitPrice = parseBigDecimal(getFormFieldValue(multipartData, "unitPrice"));
            BigDecimal minShipmentQuantity = parseBigDecimal(getFormFieldValue(multipartData, "minShipmentQuantity"));

            List<String> destinationCities = destinationCitiesRaw != null
                    ? Arrays.asList(destinationCitiesRaw.split(","))
                    : List.of();

            // 3. Subir imágenes a S3 y construir DTO
            return s3ImageStorageService.uploadImages(filePartFlux)
                    .flatMap(imageUrls -> {
                        SellDTO sellDTO = SellDTO.builder()
                                .sellerId(UUID.fromString(userId))
                                .name(name)
                                .description(description)
                                .category(category)
                                .quantity(quantity)
                                .unit(unit)
                                .unitPrice(unitPrice)
                                .minShipmentQuantity(minShipmentQuantity)
                                .originLocation(originLocation)
                                .destinationCities(destinationCities)
                                .images(imageUrls)
                                .build();

                        return sellUseCase.sellProduct(sellDTO);
                    })
                    .map(SellProductMapper::toSellResponse)
                    .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
        });
    }

    private String getFormFieldValue(org.springframework.util.MultiValueMap<String, Part> multipartData, String fieldName) {
        List<Part> parts = multipartData.get(fieldName);
        if (parts != null && !parts.isEmpty() && parts.get(0) instanceof FormFieldPart formField) {
            return formField.value();
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            return value != null ? new BigDecimal(value) : BigDecimal.ZERO;
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}