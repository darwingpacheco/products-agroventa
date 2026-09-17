package com.agro_venta.products.infrastructure.repository;

import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface SellProductR2dbRepository extends ReactiveCrudRepository<SellProducEntity, UUID> {
}
