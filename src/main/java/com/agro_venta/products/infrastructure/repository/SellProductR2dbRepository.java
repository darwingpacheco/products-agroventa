package com.agro_venta.products.infrastructure.repository;

import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface SellProductR2dbRepository extends ReactiveCrudRepository<SellProducEntity, UUID> {

    @Query("""
            SELECT * FROM products
            WHERE status = 'EN_VENTA'
              AND (:category IS NULL OR category ILIKE :category)
              AND (:destinationCity IS NULL OR :destinationCity = ANY(destination_cities))
            ORDER BY created_at DESC
            LIMIT :size OFFSET :offset
            """)
    Flux<SellProducEntity> findCatalog(String category, String destinationCity, int size, long offset);

    Flux<SellProducEntity> findBySellerId(UUID sellerId);

    /**
     * Atomic stock reservation using optimistic locking via conditional UPDATE.
     * Ensures we never over-reserve.
     */
    @Modifying
    @Query("""
            UPDATE products
            SET reserved_quantity = reserved_quantity + :qty,
                updated_at        = NOW()
            WHERE id = :id
              AND (quantity - reserved_quantity) >= :qty
            """)
    Mono<Long> reserveStock(UUID id, BigDecimal qty);

    @Modifying
    @Query("""
            UPDATE products
            SET reserved_quantity = GREATEST(reserved_quantity - :qty, 0),
                updated_at        = NOW()
            WHERE id = :id
            """)
    Mono<Long> releaseStock(UUID id, BigDecimal qty);
}
