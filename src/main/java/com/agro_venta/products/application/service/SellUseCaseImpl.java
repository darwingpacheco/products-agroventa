package com.agro_venta.products.application.service;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.application.useCase.SellUseCase;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.port.SellProductQueryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SellUseCaseImpl implements SellUseCase {

    private final SellProductQueryPort sellProductQueryPort;

    public SellUseCaseImpl(SellProductQueryPort sellProductQueryPort) {
        this.sellProductQueryPort = sellProductQueryPort;
    }

    @Override
    public Mono<SoldProduct> sellProduct(SellDTO sellDTO) {
        return sellProductQueryPort.createSellProduct(sellDTO);
    }
}
