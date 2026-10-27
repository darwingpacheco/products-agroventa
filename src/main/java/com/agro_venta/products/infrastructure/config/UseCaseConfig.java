package com.agro_venta.products.infrastructure.config;

import com.agro_venta.products.application.dto.CatalogQuery;
import com.agro_venta.products.application.service.*;
import com.agro_venta.products.application.useCase.*;
import com.agro_venta.products.domain.model.Product;
import com.agro_venta.products.domain.port.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;

@Configuration
public class UseCaseConfig {

    @Bean
    public SellUseCase sellUseCase(SellProductQueryPort sellProductQueryPort) {
        return new SellUseCaseImpl(sellProductQueryPort);
    }

    @Bean
    public CreateOrderUseCase createOrderUseCase(
            ProductQueryPort productQueryPort,
            OrderQueryPort orderQueryPort) {
        return new CreateOrderService(productQueryPort, orderQueryPort);
    }

    @Bean
    public ProcessPaymentWebhookUseCase processPaymentWebhookUseCase(
            OrderQueryPort orderQueryPort,
            PaymentPort paymentPort,
            ProductQueryPort productQueryPort,
            ShipmentPort shipmentPort,
            EventPublisherPort eventPublisher) {
        return new ProcessPaymentWebhookService(
                orderQueryPort, paymentPort, productQueryPort, shipmentPort, eventPublisher);
    }

    @Bean
    public DispatchShipmentUseCase dispatchShipmentUseCase(
            ProductQueryPort productQueryPort,
            ShipmentPort shipmentPort,
            OrderQueryPort orderQueryPort,
            EventPublisherPort eventPublisher) {
        return new DispatchShipmentService(
                productQueryPort, shipmentPort, orderQueryPort, eventPublisher);
    }

    @Bean
    public GetCatalogUseCase getCatalogUseCase(
            ProductQueryPort productQueryPort
    ){
        return new ObtainCatalogService(productQueryPort);
    }
}
