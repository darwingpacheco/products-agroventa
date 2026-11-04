package com.agro_venta.products.application.service;

import com.agro_venta.products.application.dto.WompiWebhookEvent;
import com.agro_venta.products.application.useCase.ProcessPaymentWebhookUseCase;
import com.agro_venta.products.domain.model.Payment;
import com.agro_venta.products.domain.model.Shipment;
import com.agro_venta.products.domain.port.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;


public class ProcessPaymentWebhookService implements ProcessPaymentWebhookUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessPaymentWebhookService.class);
    private static final String EVENT_TRANSACTION_UPDATED = "transaction.updated";

    private final OrderQueryPort    orderQueryPort;
    private final PaymentPort       paymentPort;
    private final ProductQueryPort  productQueryPort;
    private final ShipmentPort      shipmentPort;
    private final EventPublisherPort eventPublisher;

    public ProcessPaymentWebhookService(
            OrderQueryPort orderQueryPort,
            PaymentPort paymentPort,
            ProductQueryPort productQueryPort,
            ShipmentPort shipmentPort,
            EventPublisherPort eventPublisher) {
        this.orderQueryPort  = orderQueryPort;
        this.paymentPort     = paymentPort;
        this.productQueryPort = productQueryPort;
        this.shipmentPort    = shipmentPort;
        this.eventPublisher  = eventPublisher;
    }

    @Override
    public Mono<Void> process(WompiWebhookEvent event) {
        if (!EVENT_TRANSACTION_UPDATED.equals(event.event())) {
            return Mono.empty();
        }

        var tx = event.data().transaction();

        // Idempotency check — if already processed, skip
        return paymentPort.findByProviderTransactionId(tx.id())
                .flatMap(existing -> {
                    log.info("Webhook already processed for tx {}", tx.id());
                    return Mono.<Void>empty();
                })
                .switchIfEmpty(Mono.defer(() -> processNewTransaction(tx)));
    }

    private Mono<Void> processNewTransaction(WompiWebhookEvent.Transaction tx) {
        UUID orderId;
        try {
            // Reference format: <orderId> OR <orderId>_<timestamp> (retry attempts)
            String ref = tx.reference();
            if (ref != null && ref.contains("_")) {
                ref = ref.substring(0, ref.lastIndexOf("_"));
            }
            orderId = UUID.fromString(ref);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid orderId in Wompi reference: {}", tx.reference());
            return Mono.empty();
        }

        return orderQueryPort.findById(orderId)
                .flatMap(order -> {
                    BigDecimal amount = BigDecimal.valueOf(tx.amountInCents())
                            .divide(BigDecimal.valueOf(100));

                    Payment payment = new Payment(
                            null, orderId, "WOMPI", tx.id(),
                            amount, "COP",
                            mapWompiStatus(tx.status()),
                            tx.paymentMethodType(),
                            null,
                            OffsetDateTime.now()
                    );

                    return paymentPort.save(payment)
                            .flatMap(saved -> {
                                if ("APPROVED".equals(tx.status())) {
                                    return handleApprovedPayment(order, saved);
                                } else if ("DECLINED".equals(tx.status()) || "VOIDED".equals(tx.status())) {
                                    return handleFailedPayment(order);
                                }
                                return Mono.empty();
                            });
                })
                .doOnError(e -> log.error("Error processing payment webhook for order {}", orderId, e))
                .onErrorResume(e -> Mono.empty());
    }

    private Mono<Void> handleApprovedPayment(
            com.agro_venta.products.domain.model.Order order,
            Payment payment) {

        return orderQueryPort.updateStatus(order.id(), "PAGADO")
                .flatMap(updatedOrder ->
                        // Update shipment accumulation
                        shipmentPort.findActiveByProductId(order.productId())
                                .switchIfEmpty(
                                        // Create first shipment record for this product batch
                                        productQueryPort.findById(order.productId())
                                                .flatMap(product -> {
                                                    Shipment s = new Shipment(
                                                            null, product.id(),
                                                            BigDecimal.ZERO,
                                                            product.minShipmentQuantity(),
                                                            "ACUMULANDO",
                                                            null, null);
                                                    return shipmentPort.save(s);
                                                }))
                                .flatMap(shipment -> {
                                    BigDecimal newTotal = shipment.totalQuantity()
                                            .add(updatedOrder.quantity());
                                    Shipment updated = new Shipment(
                                            shipment.id(), shipment.productId(),
                                            newTotal,
                                            shipment.minQuantityThreshold(),
                                            newTotal.compareTo(shipment.minQuantityThreshold()) >= 0
                                                    ? "LISTO_PARA_DESPACHAR" : "ACUMULANDO",
                                            shipment.readyAt(),
                                            null);
                                    return shipmentPort.save(updated)
                                            .flatMap(saved -> shipmentPort.linkOrderToShipment(
                                                    updatedOrder.id(), saved.id())
                                                    .thenReturn(saved));
                                })
                )
                // Publish billing event (SQS → Lambda → PDF factura)
                .flatMap(shipment -> eventPublisher.publish(
                        "billing",
                        "ORDER_PAID",
                        Map.of(
                                "orderId",   order.id().toString(),
                                "buyerId",   order.buyerId().toString(),
                                "sellerId",  order.sellerId().toString(),
                                "productId", order.productId().toString(),
                                "total",     order.total().toPlainString(),
                                "paymentId", payment.id().toString()
                        )))
                // Publish notification event (SQS → Lambda → SES email)
                .then(eventPublisher.publish(
                        "notifications",
                        "ORDER_PAID",
                        Map.of(
                                "orderId",  order.id().toString(),
                                "buyerId",  order.buyerId().toString(),
                                "sellerId", order.sellerId().toString(),
                                "total",    order.total().toPlainString()
                        )));
    }

    private Mono<Void> handleFailedPayment(com.agro_venta.products.domain.model.Order order) {
        return orderQueryPort.updateStatus(order.id(), "CANCELADO")
                .flatMap(updated ->
                        productQueryPort.releaseStock(order.productId(), order.quantity()))
                .then();
    }

    private String mapWompiStatus(String wompiStatus) {
        return switch (wompiStatus) {
            case "APPROVED" -> "APROBADO";
            case "DECLINED", "VOIDED" -> "RECHAZADO";
            default -> "PENDIENTE";
        };
    }

    /**
     * App-side payment confirmation. Called by the app after the Wompi
     * checkout redirect shows APPROVED. Serves as a fallback when the
     * Wompi webhook is not configured or hasn't arrived yet.
     */
    @Override
    public Mono<Void> confirmPayment(UUID orderId, UUID buyerId) {
        return orderQueryPort.findById(orderId)
                .flatMap(order -> {
                    // Security: only the buyer can confirm their own payment
                    if (!order.buyerId().equals(buyerId)) {
                        log.warn("Buyer {} tried to confirm payment for order owned by {}",
                                buyerId, order.buyerId());
                        return Mono.empty();
                    }
                    // Idempotency: if already paid, nothing to do — return success silently
                    if ("PAGADO".equals(order.status())) {
                        log.info("Order {} already PAGADO, skipping confirmation", orderId);
                        return Mono.empty();
                    }
                    // Only confirm if still pending
                    if (!"PENDIENTE_PAGO".equals(order.status())) {
                        log.info("Order {} has status {}, skipping confirmation",
                                orderId, order.status());
                        return Mono.empty();
                    }

                    String syntheticTxId = "APP_CONFIRM_" + orderId;

                    // Check if we already created a payment record for this order
                    return paymentPort.findByProviderTransactionId(syntheticTxId)
                            .flatMap(existingPayment -> {
                                // Payment record already exists but order not yet updated —
                                // just update the order status directly
                                log.info("Payment record already exists for order {}, updating status only", orderId);
                                return handleApprovedPayment(order, existingPayment);
                            })
                            .switchIfEmpty(Mono.defer(() -> {
                                // First time confirming — create payment record then update order
                                Payment payment = new Payment(
                                        null, orderId, "WOMPI", syntheticTxId,
                                        order.total(), "COP",
                                        "APROBADO", "APP_REDIRECT", null,
                                        OffsetDateTime.now());

                                return paymentPort.save(payment)
                                        .flatMap(saved -> handleApprovedPayment(order, saved));
                            }));
                })
                .doOnError(e -> log.error("Error confirming payment for order {}", orderId, e))
                .onErrorResume(e -> Mono.empty());
    }
}
