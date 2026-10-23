package com.agro_venta.products.infrastructure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class SellProducEntity {

    @Id
    private UUID id;

    @Column("seller_id")
    private UUID sellerId;

    @Column("name")
    private String name;

    @Column("description")
    private String description;

    @Column("category")
    private String category;

    @Column("quantity")
    private BigDecimal quantity;

    @Column("reserved_quantity")
    private BigDecimal reservedQuantity;

    @Column("unit")
    private String unit;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @Column("total_price")
    private BigDecimal totalPrice;

    @Column("min_shipment_quantity")
    private BigDecimal minShipmentQuantity;

    @Column("origin_location")
    private String originLocation;

    @Column("destination_cities")
    private String[] destinationCities;

    @Column("image_urls")
    private String[] imageUrls;

    @Column("status")
    private String status;

    @Column("harvest_date")
    private LocalDate harvestDate;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
