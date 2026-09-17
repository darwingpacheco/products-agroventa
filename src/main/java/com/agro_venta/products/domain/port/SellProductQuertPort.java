package com.agro_venta.products.domain.port;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import reactor.core.publisher.Mono;

public interface SellProductQuertPort {
    Mono<SoldProduct> createSellProduct(SellDTO sellDTO);
}
