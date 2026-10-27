package com.agro_venta.products.application.service;

import com.agro_venta.products.application.dto.CatalogQuery;
import com.agro_venta.products.application.useCase.GetCatalogUseCase;
import com.agro_venta.products.domain.exception.ResourceNotFoundException;
import com.agro_venta.products.domain.model.Product;
import com.agro_venta.products.domain.port.ProductQueryPort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class ObtainCatalogService implements GetCatalogUseCase {

    private final ProductQueryPort productQueryPort;

    public ObtainCatalogService(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    @Override
    public Flux<Product> getCatalog(CatalogQuery catalogQuery) {
        return productQueryPort.findCatalog(
                        catalogQuery.category(),
                        catalogQuery.destinationCity(),
                        catalogQuery.page(),
                        catalogQuery.size())
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("No hay productos")));
    }
}
