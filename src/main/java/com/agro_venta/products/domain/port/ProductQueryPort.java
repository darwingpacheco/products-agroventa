package com.agro_venta.products.domain.port;

import com.agro_venta.products.domain.model.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductQueryPort {

    Mono<Product> findById(UUID id);

    Flux<Product> findCatalog(String category, String destinationCity, int page, int size);

    Flux<Product> findBySellerId(UUID sellerId);

    Mono<Product> save(Product product);

    Mono<Product> reserveStock(UUID productId, java.math.BigDecimal quantity);

    Mono<Product> releaseStock(UUID productId, java.math.BigDecimal quantity);
}
