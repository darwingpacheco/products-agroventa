package com.agro_venta.products.domain.port;

import com.agro_venta.products.domain.model.Shipment;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ShipmentPort {

    Mono<Shipment> findActiveByProductId(UUID productId);

    Mono<Shipment> save(Shipment shipment);

    Mono<Void> linkOrderToShipment(UUID orderId, UUID shipmentId);

    Mono<Shipment> dispatch(UUID productId, UUID sellerRequesterId);
}
