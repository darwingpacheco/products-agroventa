package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.application.useCase.GetCatalogUseCase;
import com.agro_venta.products.application.useCase.SellUseCase;
import com.agro_venta.products.infrastructure.mapper.SellProductMapper;
import com.agro_venta.products.infrastructure.service.S3ImageStorageService;
import com.agro_venta.products.interfaces.rest.dto.CatalogItemResponse;
import com.agro_venta.products.application.dto.CatalogQuery;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Handles product listing operations (create, catalog).
 * Renamed from AuthController to ProductController.
 */
@Validated
@RestController
@RequestMapping("/api/agro-venta/v1/products")
public class ProductController {

    private final SellUseCase            sellUseCase;
    private final S3ImageStorageService  s3ImageStorageService;
    private final GetCatalogUseCase getCatalogUseCase;

    public ProductController(SellUseCase sellUseCase, S3ImageStorageService s3ImageStorageService, GetCatalogUseCase getCatalogUseCase) {
        this.sellUseCase           = sellUseCase;
        this.s3ImageStorageService = s3ImageStorageService;
        this.getCatalogUseCase = getCatalogUseCase;
    }

    /**
     * Publish a product listing. Only AGRICULTOR role allowed (enforced by SecurityConfig).
     * The sellerId is extracted from the JWT principal, never from the request body.
     */
    @PostMapping(value = "/sell", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<SellResponse>> sellProduct(
            @Valid @ModelAttribute SellRequest request,
            @RequestPart(value = "images", required = false) Flux<FilePart> images,
            @AuthenticationPrincipal String userId) {

        Flux<FilePart> safeImages = images != null ? images : Flux.empty();

        return s3ImageStorageService.uploadImages(safeImages)
                .map(imageUrls -> {
                    var dto = SellProductMapper.sellInputToDTO(request, imageUrls);
                    // Inject sellerId from JWT
                    return com.agro_venta.products.application.dto.SellDTO.builder()
                            .sellerId(UUID.fromString(userId))
                            .name(dto.getName())
                            .description(dto.getDescription())
                            .category(dto.getCategory())
                            .quantity(dto.getQuantity())
                            .unit(dto.getUnit())
                            .unitPrice(dto.getUnitPrice())
                            .minShipmentQuantity(dto.getMinShipmentQuantity())
                            .originLocation(dto.getOriginLocation())
                            .destinationCities(dto.getDestinationCities())
                            .images(dto.getImages())
                            .harvestDate(dto.getHarvestDate())
                            .build();
                })
                .flatMap(sellUseCase::sellProduct)
                .map(SellProductMapper::toSellResponse)
                .map(body -> ResponseEntity.status(HttpStatus.CREATED).body(body));
    }

    @GetMapping(value = "/catalog", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CatalogItemResponse> catalog(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String destinationCity,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        return getCatalogUseCase
                .getCatalog(new CatalogQuery(category, destinationCity, search, page, size))
                .map(SellProductMapper::toCatalogResponse);
    }
}
