package com.agro_venta.products.infrastructure.persistence.adapter;

import com.agro_venta.products.domain.exception.BusinessException;
import com.agro_venta.products.domain.model.Shipment;
import com.agro_venta.products.domain.port.ProductQueryPort;
import com.agro_venta.products.domain.port.ShipmentPort;
import com.agro_venta.products.infrastructure.entity.ShipmentEntity;
import com.agro_venta.products.infrastructure.repository.ShipmentR2dbcRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Repository
public class ShipmentR2dbcAdapter implements ShipmentPort {

    private final ShipmentR2dbcRepository repository;
    private final DatabaseClient          databaseClient;
    private final ProductQueryPort        productQueryPort;

    public ShipmentR2dbcAdapter(
            ShipmentR2dbcRepository repository,
            DatabaseClient databaseClient,
            ProductQueryPort productQueryPort) {
        this.repository       = repository;
        this.databaseClient   = databaseClient;
        this.productQueryPort = productQueryPort;
    }

    @Override
    public Mono<Shipment> findActiveByProductId(UUID productId) {
        return repository.findActiveByProductId(productId).map(this::toDomain);
    }

    @Override
    public Mono<Shipment> save(Shipment shipment) {
        return repository.save(toEntity(shipment)).map(this::toDomain);
    }

    @Override
    public Mono<Void> linkOrderToShipment(UUID orderId, UUID shipmentId) {
        return databaseClient.sql(
                        "INSERT INTO order_shipments(order_id, shipment_id) VALUES(:orderId, :shipmentId) ON CONFLICT DO NOTHING")
                .bind("orderId", orderId)
                .bind("shipmentId", shipmentId)
                .fetch().rowsUpdated().then();
    }

    @Override
    public Mono<Shipment> dispatch(UUID productId, UUID sellerRequesterId) {
        return productQueryPort.findById(productId)
                .switchIfEmpty(Mono.error(new BusinessException("Producto no encontrado")))
                .flatMap(product -> {
                    if (!product.sellerId().equals(sellerRequesterId)) {
                        return Mono.error(new BusinessException("Sin permiso para despachar"));
                    }
                    return findActiveByProductId(productId)
                            .switchIfEmpty(Mono.error(new BusinessException(
                                    "No hay carga acumulada para este producto")))
                            .flatMap(shipment -> {
                                if (!"LISTO_PARA_DESPACHAR".equals(shipment.status())) {
                                    return Mono.error(new BusinessException(
                                            "La carga no ha alcanzado el mínimo requerido"));
                                }
                                Shipment dispatched = new Shipment(
                                        shipment.id(), shipment.productId(),
                                        shipment.totalQuantity(), shipment.minQuantityThreshold(),
                                        "DESPACHADO", shipment.readyAt(), OffsetDateTime.now());
                                return save(dispatched);
                            });
                });
    }

    private Shipment toDomain(ShipmentEntity e) {
        return new Shipment(
                e.getId(), e.getProductId(),
                e.getTotalQuantity(), e.getMinQuantityThreshold(),
                e.getStatus(), e.getReadyAt(), e.getDispatchedAt());
    }

    private ShipmentEntity toEntity(Shipment s) {
        return ShipmentEntity.builder()
                .id(s.id())
                .productId(s.productId())
                .totalQuantity(s.totalQuantity())
                .minQuantityThreshold(s.minQuantityThreshold())
                .status(s.status())
                .readyAt(s.readyAt())
                .dispatchedAt(s.dispatchedAt())
                .build();
    }
}
