package com.agro_venta.products.domain.port;

import com.agro_venta.products.domain.model.Order;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderQueryPort {

    Mono<Order> save(Order order);

    Mono<Order> findById(UUID id);

    Flux<Order> findByBuyerId(UUID buyerId, int page, int size);

    Flux<Order> findBySellerIdAndStatus(UUID sellerId, String status);

    Flux<Order> findPaidOrdersByProductId(UUID productId);

    Mono<Order> updateStatus(UUID orderId, String status);
}
