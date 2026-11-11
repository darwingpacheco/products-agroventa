package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.application.dto.CreateOrderCommand;
import com.agro_venta.products.application.useCase.CreateOrderUseCase;
import com.agro_venta.products.domain.exception.BusinessException;
import com.agro_venta.products.domain.port.OrderQueryPort;
import com.agro_venta.products.domain.port.ProductQueryPort;
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
    private final ProductQueryPort   productQueryPort;

    public OrderController(
            CreateOrderUseCase createOrderUseCase,
            OrderQueryPort orderQueryPort,
            ProductQueryPort productQueryPort) {
        this.createOrderUseCase = createOrderUseCase;
        this.orderQueryPort     = orderQueryPort;
        this.productQueryPort   = productQueryPort;
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

    /** Get current user's orders (as buyer). */
    @GetMapping("/my-orders")
    public Flux<OrderResponse> getMyOrders(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return orderQueryPort.findByBuyerId(UUID.fromString(userId), page, size)
                .map(OrderResponse::fromDomain);
    }

    /**
     * Get orders received as seller (AGRICULTOR).
     * Returns all orders where the authenticated user is the seller.
     */
    @GetMapping("/my-sales")
    public Flux<OrderResponse> getMySales(
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "PAGADO") String status) {
        return orderQueryPort.findBySellerIdAndStatus(UUID.fromString(userId), status)
                .map(OrderResponse::fromDomain);
    }

    /**
     * Cancel a pending order. Only the buyer can cancel their own order,
     * and only if the order is still in PENDIENTE_PAGO status.
     * Cancelling releases the reserved stock back to the catalog.
     */
    @PostMapping("/{id}/cancel")
    public Mono<ResponseEntity<OrderResponse>> cancelOrder(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {
        return orderQueryPort.findById(id)
                // Security: only the buyer can cancel
                .filter(order -> order.buyerId().toString().equals(userId))
                .switchIfEmpty(Mono.error(new BusinessException(
                        "Pedido no encontrado o no tienes permiso para cancelarlo")))
                .flatMap(order -> {
                    // Business rule: only pending orders can be cancelled
                    if (!"PENDIENTE_PAGO".equals(order.status())) {
                        return Mono.<com.agro_venta.products.domain.model.Order>error(
                                new BusinessException("Solo se pueden cancelar pedidos pendientes de pago"));
                    }
                    // Update order status to CANCELADO and release reserved stock
                    return orderQueryPort.updateStatus(order.id(), "CANCELADO")
                            .flatMap(updated ->
                                    productQueryPort.releaseStock(order.productId(), order.quantity())
                                            .thenReturn(updated));
                })
                .map(order -> ResponseEntity.ok(OrderResponse.fromDomain(order)))
                .onErrorResume(e -> {
                    if (e instanceof BusinessException) {
                        return Mono.just(ResponseEntity
                                .badRequest()
                                .build());
                    }
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }
}
