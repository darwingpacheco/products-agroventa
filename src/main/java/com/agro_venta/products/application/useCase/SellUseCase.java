package com.agro_venta.products.application.useCase;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import reactor.core.publisher.Mono;

public interface SellUseCase {

    Mono<SoldProduct> sellProduct(SellDTO sellDTO);
}
