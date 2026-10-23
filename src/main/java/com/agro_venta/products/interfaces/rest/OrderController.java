package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.application.dto.CreateOrderCommand;
import com.agro_venta.products.application.useCase.CreateOrderUseCase;
import com.agro_venta.products.domain.port.OrderQueryPort;
import com.agro_venta.products.interfaces.rest.dto.CreateOrderRequest;
import com.agro_venta.products.interfaces.rest.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/agro-venta/v1/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final OrderQueryPort     orderQueryPort;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            OrderQueryPort orderQueryPort) {
        this.createOrderUseCase = createOrderUseCase;
        this.orderQueryPort     = orderQueryPort;
    }

    /** Create a new order. Only COMPRADOR role (enforced by SecurityConfig). */
    @PostMapping
    public Mono<ResponseEntity<OrderResponse>> createOrder(
            @Valid @RequestBody Mono<CreateOrderRequest> requestMono,
            @AuthenticationPrincipal String userId) {
        return requestMono
                .map(req -> new CreateOrderCommand(
                        req.productId(),
                        UUID.fromString(userId),
                        req.quantity(),
                        req.destinationCity(),
                        req.shippingAddress()))
                .flatMap(createOrderUseCase::createOrder)
                .map(order -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(OrderResponse.fromDomain(order)));
    }

    /** Get an order by id. User must own the order as buyer or seller. */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<OrderResponse>> getOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        return orderQueryPort.findById(id)
                .filter(order -> order.buyerId().toString().equals(userId)
                        || order.sellerId().toString().equals(userId))
                .map(order -> ResponseEntity.ok(OrderResponse.fromDomain(order)))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    /** Get current user's orders (as buyer or seller). */
    @GetMapping("/my-orders")
    public Flux<OrderResponse> getMyOrders(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderQueryPort.findByBuyerId(UUID.fromString(userId), page, size)
                .map(OrderResponse::fromDomain);
    }
}
