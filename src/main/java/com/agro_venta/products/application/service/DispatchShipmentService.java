package com.agro_venta.products.application.service;

import com.agro_venta.products.application.useCase.DispatchShipmentUseCase;
import com.agro_venta.products.domain.exception.BusinessException;
import com.agro_venta.products.domain.exception.ResourceNotFoundException;
import com.agro_venta.products.domain.model.Shipment;
import com.agro_venta.products.domain.port.EventPublisherPort;
import com.agro_venta.products.domain.port.OrderQueryPort;
import com.agro_venta.products.domain.port.ProductQueryPort;
import com.agro_venta.products.domain.port.ShipmentPort;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class DispatchShipmentService implements DispatchShipmentUseCase {

    private final ProductQueryPort  productQueryPort;
    private final ShipmentPort      shipmentPort;
    private final OrderQueryPort    orderQueryPort;
    private final EventPublisherPort eventPublisher;

    public DispatchShipmentService(
            ProductQueryPort productQueryPort,
            ShipmentPort shipmentPort,
            OrderQueryPort orderQueryPort,
            EventPublisherPort eventPublisher) {
        this.productQueryPort = productQueryPort;
        this.shipmentPort     = shipmentPort;
        this.orderQueryPort   = orderQueryPort;
        this.eventPublisher   = eventPublisher;
    }

    @Override
    public Mono<Shipment> dispatch(UUID productId, UUID sellerRequesterId) {
        return productQueryPort.findById(productId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Producto no encontrado")))
                .flatMap(product -> {
                    // Validate ownership
                    if (!product.sellerId().equals(sellerRequesterId)) {
                        return Mono.error(new BusinessException(
                                "No tienes permiso para despachar este producto"));
                    }
                    return shipmentPort.findActiveByProductId(productId)
                            .switchIfEmpty(Mono.error(
                                    new BusinessException("No hay carga acumulada para este producto")));
                })
                .flatMap(shipment -> {
                    if (!"LISTO_PARA_DESPACHAR".equals(shipment.status())) {
                        return Mono.error(new BusinessException(
                                "La carga aún no alcanza el mínimo requerido para despachar. " +
                                "Acumulado: " + shipment.totalQuantity() +
                                " / Mínimo: " + shipment.minQuantityThreshold()));
                    }

                    Shipment dispatched = new Shipment(
                            shipment.id(), shipment.productId(),
                            shipment.totalQuantity(), shipment.minQuantityThreshold(),
                            "DESPACHADO", shipment.readyAt(), OffsetDateTime.now());

                    return shipmentPort.save(dispatched);
                })
                // Update all paid orders in this shipment to DESPACHADO
                .flatMap(dispatched ->
                        orderQueryPort.findPaidOrdersByProductId(productId)
                                .flatMap(order -> orderQueryPort.updateStatus(order.id(), "DESPACHADO")
                                        .flatMap(updated -> eventPublisher.publish(
                                                "notifications",
                                                "ORDER_DISPATCHED",
                                                Map.of(
                                                        "orderId", order.id().toString(),
                                                        "buyerId", order.buyerId().toString()
                                                )))
                                )
                                .then(Mono.just(dispatched)));
    }
}
