package com.agro_venta.products.interfaces.rest;

import com.agro_venta.products.application.useCase.DispatchShipmentUseCase;
import com.agro_venta.products.domain.model.Shipment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Shipment management for farmers (AGRICULTOR role).
 * A shipment groups all paid orders for a product batch and is dispatched
 * when the minimum load threshold is reached.
 */
@RestController
@RequestMapping("/api/agro-venta/v1/shipments")
public class ShipmentController {

    private final DispatchShipmentUseCase dispatchShipmentUseCase;

    public ShipmentController(DispatchShipmentUseCase dispatchShipmentUseCase) {
        this.dispatchShipmentUseCase = dispatchShipmentUseCase;
    }

    /**
     * Confirm dispatch of a product batch.
     * Requires: AGRICULTOR role + ownership of the product.
     * All PAGADO orders for this product will move to DESPACHADO.
     */
    @PatchMapping("/{productId}/dispatch")
    public Mono<ResponseEntity<ShipmentResponse>> dispatchProduct(
            @PathVariable UUID productId,
            @AuthenticationPrincipal String userId) {
        return dispatchShipmentUseCase.dispatch(productId, UUID.fromString(userId))
                .map(shipment -> ResponseEntity.ok(ShipmentResponse.fromDomain(shipment)));
    }

    record ShipmentResponse(
            UUID   id,
            UUID   productId,
            java.math.BigDecimal totalQuantity,
            java.math.BigDecimal minQuantityThreshold,
            String status,
            java.time.OffsetDateTime dispatchedAt
    ) {
        static ShipmentResponse fromDomain(Shipment s) {
            return new ShipmentResponse(
                    s.id(), s.productId(),
                    s.totalQuantity(), s.minQuantityThreshold(),
                    s.status(), s.dispatchedAt());
        }
    }
}
