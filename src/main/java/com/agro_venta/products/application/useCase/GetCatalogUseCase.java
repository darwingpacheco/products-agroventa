package com.agro_venta.products.application.useCase;

import com.agro_venta.products.domain.model.Product;
import com.agro_venta.products.application.dto.CatalogQuery;
import reactor.core.publisher.Flux;

public interface GetCatalogUseCase {
    Flux<Product> getCatalog(CatalogQuery catalogQuery);
}
