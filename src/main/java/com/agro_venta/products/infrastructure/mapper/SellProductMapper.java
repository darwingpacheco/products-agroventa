package com.agro_venta.products.infrastructure.mapper;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.utils.enums.SellStatusEnum;
import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;

import java.util.Collections;
import java.util.List;
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

    public static SoldProduct sellEntityToDomain(SellProducEntity entity) {
        return new SoldProduct(
                entity.getId(),
                entity.getSellerId(),
                entity.getName(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getQuantity(),
                entity.getUnit(),
                entity.getUnitPrice(),
                entity.getTotalPrice(),
                entity.getOriginLocation(),
                entity.getImageUrls() == null ? Collections.emptyList() : entity.getImageUrls(),
                "El producto se ha publicado exitosamente",
                entity.getStatus()
        );
    }

    public static SellDTO sellInputToDTO(SellRequest request, List<String> imageUrls){
        return SellDTO.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .quantity(request.quantity())
                .unit(request.unit())
                .unitPrice(request.unitPrice())
                .originLocation(request.originLocation())
                .images(imageUrls)
                .build();
    }

    public static SellDTO sellInputToDTO(SellRequest request){
        return sellInputToDTO(request, List.of());
    }

    public static SellResponse toSellResponse(SoldProduct soldProduct) {
        return new SellResponse(
                soldProduct.id(),
                soldProduct.sellerId(),
                soldProduct.name(),
                soldProduct.description(),
                soldProduct.category(),
                soldProduct.quantity(),
                soldProduct.unit(),
                soldProduct.unitPrice(),
                soldProduct.totalPrice(),
                soldProduct.originLocation(),
                soldProduct.imageUrls(),
                soldProduct.message(),
                soldProduct.status()
        );
    }
}
