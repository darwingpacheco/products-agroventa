package com.agro_venta.products.domain.port;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import reactor.core.publisher.Mono;

/** Port for creating a product listing. Renamed from SellProductQuertPort (typo fixed). */
public interface SellProductQueryPort extends SellProductQuertPort {
    // Extends the old interface for backwards compatibility during migration
}
