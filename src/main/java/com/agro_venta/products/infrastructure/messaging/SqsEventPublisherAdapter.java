package com.agro_venta.products.infrastructure.messaging;

import com.agro_venta.products.domain.port.EventPublisherPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class SqsEventPublisherAdapter implements EventPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(SqsEventPublisherAdapter.class);

    private final SqsClient    sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${app.aws.sqs.notifications-queue-url:}")
    private String notificationsQueueUrl;

    @Value("${app.aws.sqs.billing-queue-url:}")
    private String billingQueueUrl;

    public SqsEventPublisherAdapter(SqsClient sqsClient, ObjectMapper objectMapper) {
        this.sqsClient    = sqsClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> publish(String queueName, String eventType, Map<String, Object> payload) {
        String queueUrl = resolveQueueUrl(queueName);
        if (queueUrl == null || queueUrl.isBlank()) {
            log.warn("SQS queue URL not configured for '{}' — skipping event", queueName);
            return Mono.empty();
        }

        Map<String, Object> envelope = new HashMap<>(payload);
        envelope.put("eventType", eventType);

        return Mono.fromCallable(() -> {
                    String body = objectMapper.writeValueAsString(envelope);
                    sqsClient.sendMessage(SendMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .messageBody(body)
                            .build());
                    log.debug("Published {} event to {} queue", eventType, queueName);
                    return body;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Failed to publish event {} to {}", eventType, queueName, e))
                .onErrorResume(e -> Mono.empty())   // don't fail business flow on messaging errors
                .then();
    }

    private String resolveQueueUrl(String queueName) {
        return switch (queueName.toLowerCase()) {
            case "notifications" -> notificationsQueueUrl;
            case "billing"       -> billingQueueUrl;
            default -> null;
        };
    }
}
