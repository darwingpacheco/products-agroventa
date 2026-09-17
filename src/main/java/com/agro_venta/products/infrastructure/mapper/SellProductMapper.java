package com.agro_venta.products.infrastructure.mapper;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.utils.enums.SellStatusEnum;
import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;

import java.util.UUID;

public class SellProductMapper {

    public static SellProducEntity sellDtoToEntiy(SellDTO sellDTO){
        SellProducEntity sellProducEntity = new SellProducEntity();
        sellProducEntity.setSellerId(UUID.randomUUID());
        sellProducEntity.setName(sellDTO.getName());
        sellProducEntity.setDescription(sellDTO.getDescription());
        sellProducEntity.setCategory(sellDTO.getCategory());
        sellProducEntity.setQuantity(sellDTO.getQuantity());
        sellProducEntity.setUnit(sellDTO.getUnit());
        sellProducEntity.setUnitPrice(sellDTO.getUnitPrice());
        sellProducEntity.setTotalPrice(
                sellDTO.getUnitPrice().multiply(sellDTO.getQuantity()));
        sellProducEntity.setOriginLocation(sellDTO.getOriginLocation());
        sellProducEntity.setImageUrls(sellDTO.getImages());
        sellProducEntity.setStatus(SellStatusEnum.EN_VENTA.toString());
        return sellProducEntity;
    }

    public static SellDTO sellInputToDTO(SellRequest request){
        return SellDTO.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .quantity(request.quantity())
                .unit(request.unit())
                .unitPrice(request.unitPrice())
                .originLocation(request.originLocation())
                .images(request.images())
                .build();
    }

    public static SellResponse toSellResponse(SoldProduct soldProduct) {
        return new SellResponse(
                soldProduct.id(),
                SellStatusEnum.EN_VENTA.toString(),
                soldProduct.status()
        );
    }
}
