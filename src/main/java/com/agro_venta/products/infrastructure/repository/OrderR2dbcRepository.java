package com.agro_venta.products.infrastructure.repository;

import com.agro_venta.products.infrastructure.entity.OrderEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderR2dbcRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

    @Query("SELECT * FROM orders WHERE buyer_id = :buyerId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<OrderEntity> findByBuyerIdPaged(UUID buyerId, int size, long offset);

    @Query("SELECT * FROM orders WHERE seller_id = :sellerId AND status = :status ORDER BY created_at DESC")
    Flux<OrderEntity> findBySellerIdAndStatus(UUID sellerId, String status);

    @Query("SELECT * FROM orders WHERE product_id = :productId AND status = 'PAGADO'")
    Flux<OrderEntity> findPaidByProductId(UUID productId);
}
