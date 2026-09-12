package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.infrastructure.mapper.SellProductMapper;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;
import com.agro_venta.products.application.useCase.SellUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@RestController
@RequestMapping("/api/agro-venta/v1/products")
public class AuthController {

    private final SellUseCase sellUseCase;

    public AuthController(SellUseCase sellUseCase) {
        this.sellUseCase = sellUseCase;
    }

    @PostMapping("/sell")
    public Mono<ResponseEntity<SellResponse>> sellProduct(@Valid @RequestBody Mono<SellRequest> monoRequest){
        return monoRequest
                .map(SellProductMapper::sellInputToDTO)
                .flatMap(sellUseCase::sellProduct)
                .map(SellProductMapper::toSellResponse)
                .map(body -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(body));
    }
}
