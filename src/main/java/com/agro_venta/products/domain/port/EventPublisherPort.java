package com.agro_venta.products.domain.port;

import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Port for publishing domain events to SQS.
 * Decouples business logic from the messaging infrastructure.
 */
public interface EventPublisherPort {

    Mono<Void> publish(String queueName, String eventType, Map<String, Object> payload);
}
