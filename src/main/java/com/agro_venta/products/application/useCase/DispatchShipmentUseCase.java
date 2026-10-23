package com.agro_venta.products.application.useCase;

import com.agro_venta.products.domain.model.Shipment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface DispatchShipmentUseCase {
    Mono<Shipment> dispatch(UUID productId, UUID sellerRequesterId);
}
