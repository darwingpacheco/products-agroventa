package com.agro_venta.products.infrastructure.mapper;

import com.agro_venta.products.application.dto.SellDTO;
import com.agro_venta.products.domain.model.SoldProduct;
import com.agro_venta.products.domain.utils.enums.SellStatusEnum;
import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import com.agro_venta.products.interfaces.rest.dto.SellRequest;
import com.agro_venta.products.interfaces.rest.dto.SellResponse;

import java.util.Collections;
import java.util.List;

public class SellProductMapper {

    private SellProductMapper() {}

    public static SellProducEntity sellDtoToEntity(SellDTO sellDTO) {
        SellProducEntity entity = new SellProducEntity();
        entity.setSellerId(sellDTO.getSellerId());
        entity.setName(sellDTO.getName());
        entity.setDescription(sellDTO.getDescription());
        entity.setCategory(sellDTO.getCategory());
        entity.setQuantity(sellDTO.getQuantity());
        entity.setReservedQuantity(java.math.BigDecimal.ZERO);
        entity.setUnit(sellDTO.getUnit());
        entity.setUnitPrice(sellDTO.getUnitPrice());
        entity.setTotalPrice(sellDTO.getUnitPrice().multiply(sellDTO.getQuantity()));
        entity.setMinShipmentQuantity(sellDTO.getMinShipmentQuantity() != null
                ? sellDTO.getMinShipmentQuantity() : sellDTO.getQuantity());
        entity.setOriginLocation(sellDTO.getOriginLocation());
        entity.setDestinationCities(
                sellDTO.getDestinationCities() != null
                        ? sellDTO.getDestinationCities().toArray(new String[0])
                        : new String[0]);

        entity.setImageUrls(
                sellDTO.getImages() != null
                        ? sellDTO.getImages().toArray(String[]::new)
                        : new String[0]);

        entity.setStatus(SellStatusEnum.EN_VENTA.toString());
        entity.setHarvestDate(sellDTO.getHarvestDate());
        return entity;
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
                arrayToList(entity.getImageUrls()),
                "El producto se ha publicado exitosamente",
                entity.getStatus()
        );
    }

    public static SellDTO sellInputToDTO(SellRequest request, List<String> imageUrls) {
        return SellDTO.builder()
                .name(request.name())
                .description(request.description())
                .category(request.category())
                .quantity(request.quantity())
                .unit(request.unit())
                .unitPrice(request.unitPrice())
                .minShipmentQuantity(request.minShipmentQuantity())
                .originLocation(request.originLocation())
                .destinationCities(request.destinationCities())
                .images(imageUrls)
                .harvestDate(request.harvestDate())
                .build();
    }

    public static SellDTO sellInputToDTO(SellRequest request) {
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

    private static List<String> arrayToList(String[] arr) {
        if (arr == null) return Collections.emptyList();
        return List.of(arr);
    }
}
