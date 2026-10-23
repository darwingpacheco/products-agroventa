package com.agro_venta.products.infrastructure.persistence.adapter;

import com.agro_venta.products.domain.model.Order;
import com.agro_venta.products.domain.port.OrderQueryPort;
import com.agro_venta.products.infrastructure.entity.OrderEntity;
import com.agro_venta.products.infrastructure.repository.OrderR2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class OrderR2dbcAdapter implements OrderQueryPort {

    private final OrderR2dbcRepository repository;
    private final DatabaseClient       databaseClient;

    public OrderR2dbcAdapter(OrderR2dbcRepository repository, DatabaseClient databaseClient) {
        this.repository     = repository;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Order> save(Order order) {
        return repository.save(toEntity(order)).map(this::toDomain);
    }

    @Override
    public Mono<Order> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Order> findByBuyerId(UUID buyerId, int page, int size) {
        return repository.findByBuyerIdPaged(buyerId, size, (long) page * size)
                .map(this::toDomain);
    }

    @Override
    public Flux<Order> findBySellerIdAndStatus(UUID sellerId, String status) {
        return repository.findBySellerIdAndStatus(sellerId, status).map(this::toDomain);
    }

    @Override
    public Flux<Order> findPaidOrdersByProductId(UUID productId) {
        return repository.findPaidByProductId(productId).map(this::toDomain);
    }

    @Override
    public Mono<Order> updateStatus(UUID orderId, String status) {
        return databaseClient.sql(
                        "UPDATE orders SET status = :status, updated_at = NOW() WHERE id = :id")
                .bind("status", status)
                .bind("id", orderId)
                .fetch().rowsUpdated()
                .then(repository.findById(orderId).map(this::toDomain));
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    private Order toDomain(OrderEntity e) {
        return new Order(
                e.getId(), e.getProductId(), e.getBuyerId(), e.getSellerId(),
                e.getQuantity(), e.getUnitPrice(), e.getSubtotal(),
                e.getShippingCost(), e.getTotal(),
                e.getDestinationCity(), e.getShippingAddress(),
                e.getStatus(), e.getPaymentStatus(),
                e.getReservationExpiresAt(), e.getCreatedAt());
    }

    private OrderEntity toEntity(Order o) {
        return OrderEntity.builder()
                .id(o.id())
                .productId(o.productId())
                .buyerId(o.buyerId())
                .sellerId(o.sellerId())
                .quantity(o.quantity())
                .unitPrice(o.unitPrice())
                .subtotal(o.subtotal())
                .shippingCost(o.shippingCost())
                .total(o.total())
                .destinationCity(o.destinationCity())
                .shippingAddress(o.shippingAddress())
                .status(o.status())
                .paymentStatus(o.paymentStatus())
                .reservationExpiresAt(o.reservationExpiresAt())
                .build();
    }
}
