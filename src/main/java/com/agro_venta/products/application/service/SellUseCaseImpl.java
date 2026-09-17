package com.agro_venta.products.application.service;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.application.useCase.SellUseCase;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.port.SellProductQuertPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SellUseCaseImpl implements SellUseCase {

    private final SellProductQuertPort sellProductQuertPort;

    public SellUseCaseImpl(SellProductQuertPort sellProductQuertPort) {
        this.sellProductQuertPort = sellProductQuertPort;
    }

    @Override
    public Mono<SoldProduct> sellProduct(SellDTO sellDTO) {
        return sellProductQuertPort.createSellProduct(sellDTO);
    }
}
