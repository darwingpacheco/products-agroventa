package com.agro_venta.products.infrastructure.repository;

import com.agro_venta.products.infrastructure.entity.ShipmentEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShipmentR2dbcRepository extends ReactiveCrudRepository<ShipmentEntity, UUID> {

    @Query("SELECT * FROM shipments WHERE product_id = :productId AND status IN ('ACUMULANDO','LISTO_PARA_DESPACHAR') LIMIT 1")
    Mono<ShipmentEntity> findActiveByProductId(UUID productId);
}
