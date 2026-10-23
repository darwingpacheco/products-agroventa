package com.agro_venta.products.infrastructure.persistence.adapter;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.port.SellProductQueryPort;
import com.agro_venta.products.infrastructure.mapper.SellProductMapper;
import com.agro_venta.products.infrastructure.repository.SellProductR2dbRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class SellProducR2dbcAdapter implements SellProductQueryPort {

    private final SellProductR2dbRepository sellProductR2dbRepository;

    public SellProducR2dbcAdapter(SellProductR2dbRepository sellProductR2dbRepository) {
        this.sellProductR2dbRepository = sellProductR2dbRepository;
    }

    @Override
    public Mono<SoldProduct> createSellProduct(SellDTO sellDTO) {
        // sellerId is now set by the caller (from JWT), not randomly generated here
        return sellProductR2dbRepository.save(SellProductMapper.sellDtoToEntity(sellDTO))
                .map(SellProductMapper::sellEntityToDomain);
    }
}
