package com.agro_venta.products.infrastructure.persistence.adapter;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.port.SellProductQuertPort;
import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import com.agro_venta.products.infrastructure.mapper.SellProductMapper;
import com.agro_venta.products.infrastructure.repository.SellProductR2dbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static com.agro_venta.products.domain.utils.constants.SellProductUtils.SUCESSFULL_SELL_PRODUCT;

@Repository
@RequiredArgsConstructor
@Component
public class SellProducR2dbcAdapter implements SellProductQuertPort {

    private final SellProductR2dbRepository sellProductR2dbRepository;

    @Override
    public Mono<SoldProduct> createSellProduct(SellDTO sellDTO) {
        return sellProductR2dbRepository.save(SellProductMapper.sellDtoToEntiy(sellDTO))
                .map(response -> new SoldProduct(
                        response.getId(),
                        SUCESSFULL_SELL_PRODUCT,
                        response.getStatus()
                ));
    }

}
