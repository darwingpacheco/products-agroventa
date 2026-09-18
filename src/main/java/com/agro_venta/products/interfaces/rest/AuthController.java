package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.infrastructure.mapper.SellProductMapper;
import com.agro_venta.products.infrastructure.service.S3ImageStorageService;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;
import com.agro_venta.products.application.useCase.SellUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

@Validated
@RestController
@RequestMapping("/api/agro-venta/v1/products")
public class AuthController {

    private final SellUseCase sellUseCase;
    private final S3ImageStorageService s3ImageStorageService;

    public AuthController(SellUseCase sellUseCase, S3ImageStorageService s3ImageStorageService) {
        this.sellUseCase = sellUseCase;
        this.s3ImageStorageService = s3ImageStorageService;
    }

    @PostMapping(value = "/sell", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<SellResponse>> sellProduct(
            @Valid @ModelAttribute SellRequest request,
            @RequestPart("images") Flux<FilePart> images) {
        return s3ImageStorageService.uploadImages(images)
                .map(imageUrls -> SellProductMapper.sellInputToDTO(request, imageUrls))
                .flatMap(sellUseCase::sellProduct)
                .map(SellProductMapper::toSellResponse)
                .map(body -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(body));
    }
}
