package com.agro_venta.products.application.service;

import com.agro_venta.products.application.dto.CatalogQuery;
import com.agro_venta.products.application.dto.CreateOrderCommand;
import com.agro_venta.products.application.useCase.CreateOrderUseCase;
import com.agro_venta.products.application.useCase.GetCatalogUseCase;
import com.agro_venta.products.domain.exception.BusinessException;
import com.agro_venta.products.domain.exception.InsufficientStockException;
import com.agro_venta.products.domain.exception.ResourceNotFoundException;
import com.agro_venta.products.domain.model.Order;
import com.agro_venta.products.domain.model.Product;
import com.agro_venta.products.domain.port.OrderQueryPort;
import com.agro_venta.products.domain.port.ProductQueryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


public class CreateOrderService implements CreateOrderUseCase {

    /**
     * Flat shipping rate per city (COP). In production this could be a table.
     */
    private static final BigDecimal SHIPPING_RATE = BigDecimal.valueOf(15_000);
    /**
     * Reservation TTL — 30 minutes
     */
    private static final int RESERVATION_MINUTES = 30;

    private final ProductQueryPort productQueryPort;
    private final OrderQueryPort orderQueryPort;

    public CreateOrderService(ProductQueryPort productQueryPort, OrderQueryPort orderQueryPort) {
        this.productQueryPort = productQueryPort;
        this.orderQueryPort = orderQueryPort;
    }

    @Override
    public Mono<Order> createOrder(CreateOrderCommand cmd) {
        return productQueryPort.findById(cmd.productId())
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Producto no encontrado")))
                .flatMap(product -> {
                    // Validate product is available
                    if (!"EN_VENTA".equals(product.status())) {
                        return Mono.error(new BusinessException(
                                "El producto no está disponible para compra"));
                    }

                    // Validate destination city
                    if (!product.destinationCities().contains(cmd.destinationCity())) {
                        return Mono.error(new BusinessException(
                                "Ciudad de destino no disponible para este producto"));
                    }

                    // Validate buyer != seller
                    if (product.sellerId().equals(cmd.buyerId())) {
                        return Mono.error(new BusinessException(
                                "No puedes comprar tu propio producto"));
                    }

                    // Check available stock
                    BigDecimal available = product.quantity().subtract(product.reservedQuantity());
                    if (available.compareTo(cmd.quantity()) < 0) {
                        return Mono.error(new InsufficientStockException(
                                "Stock insuficiente. Disponible: " + available + " " + product.unit()));
                    }

                    BigDecimal subtotal = product.unitPrice().multiply(cmd.quantity());
                    BigDecimal shippingCost = SHIPPING_RATE;
                    BigDecimal total = subtotal.add(shippingCost);

                    Order newOrder = new Order(
                            null,
                            product.id(),
                            cmd.buyerId(),
                            product.sellerId(),
                            cmd.quantity(),
                            product.unitPrice(),
                            subtotal,
                            shippingCost,
                            total,
                            cmd.destinationCity(),
                            cmd.shippingAddress(),
                            "PENDIENTE_PAGO",
                            "PENDIENTE",
                            OffsetDateTime.now().plusMinutes(RESERVATION_MINUTES),
                            null
                    );

                    return orderQueryPort.save(newOrder)
                            .flatMap(savedOrder ->
                                    productQueryPort.reserveStock(product.id(), cmd.quantity())
                                            .thenReturn(savedOrder));
                });
    }
}
