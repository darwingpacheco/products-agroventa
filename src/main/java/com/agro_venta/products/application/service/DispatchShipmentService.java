package com.agro_venta.products.application.service;

import com.agro_venta.products.application.useCase.DispatchShipmentUseCase;
import com.agro_venta.products.domain.exception.BusinessException;
import com.agro_venta.products.domain.exception.ResourceNotFoundException;
import com.agro_venta.products.domain.model.Shipment;
import com.agro_venta.products.domain.port.EventPublisherPort;
import com.agro_venta.products.domain.port.OrderQueryPort;
import com.agro_venta.products.domain.port.ProductQueryPort;
import com.agro_venta.products.domain.port.ShipmentPort;
import org.springframework.stereotype.Service;
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
                    // Calculate accumulated quantity directly from PAGADO orders
                    // (don't rely on shipment record which may not have been updated)
                    return orderQueryPort.findPaidOrdersByProductId(productId)
                            .collectList()
                            .flatMap(paidOrders -> {
                                if (paidOrders.isEmpty()) {
                                    return Mono.error(new BusinessException(
                                            "No hay pedidos pagados para despachar"));
                                }
                                java.math.BigDecimal accumulated = java.math.BigDecimal.ZERO;
                                for (var o : paidOrders) {
                                    accumulated = accumulated.add(o.quantity());
                                }
                                java.math.BigDecimal threshold = product.minShipmentQuantity();
                                if (accumulated.compareTo(threshold) < 0) {
                                    return Mono.error(new BusinessException(
                                            "La carga acumulada (" + accumulated
                                            + ") no alcanza el mínimo (" + threshold + ")"));
                                }
                                // Find or create shipment record
                                return shipmentPort.findActiveByProductId(productId)
                                        .switchIfEmpty(Mono.defer(() -> {
                                            Shipment s = new Shipment(
                                                    null, product.id(), accumulated,
                                                    threshold, "LISTO_PARA_DESPACHAR",
                                                    null, null);
                                            return shipmentPort.save(s);
                                        }))
                                        .flatMap(shipment -> {
                                            Shipment dispatched = new Shipment(
                                                    shipment.id(), shipment.productId(),
                                                    accumulated, threshold,
                                                    "DESPACHADO",
                                                    shipment.readyAt(),
                                                    OffsetDateTime.now());
                                            return shipmentPort.save(dispatched);
                                        });
                            });
                })
                // Update all paid orders for this product to DESPACHADO
                .flatMap(dispatched ->
                        orderQueryPort.findPaidOrdersByProductId(productId)
                                .flatMap(order -> orderQueryPort.updateStatus(order.id(), "DESPACHADO")
                                        .flatMap(updated -> eventPublisher.publish(
                                                "notifications",
                                                "ORDER_DISPATCHED",
                                                Map.of(
                                                        "orderId", order.id().toString(),
                                                        "buyerId", order.buyerId().toString()
                                                ))))
                                .then(Mono.just(dispatched)));
    }
}
