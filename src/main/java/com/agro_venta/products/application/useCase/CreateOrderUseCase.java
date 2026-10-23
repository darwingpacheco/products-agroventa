package com.agro_venta.products.application.useCase;

import com.agro_venta.products.application.dto.CreateOrderCommand;
import com.agro_venta.products.domain.model.Order;
import reactor.core.publisher.Mono;

public interface CreateOrderUseCase {
    Mono<Order> createOrder(CreateOrderCommand command);
}
