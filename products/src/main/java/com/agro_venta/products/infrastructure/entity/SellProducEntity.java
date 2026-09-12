package com.agro_venta.products.infrastructure.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
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

    private String name;
    private String description;
    private String category;
    private BigDecimal quantity;
    private String unit;

    @Column("unit_price")
    private BigDecimal unitPrice;

    @Column("total_price")
    private BigDecimal totalPrice;

    @Column("origin_location")
    private String originLocation;

    @Column("image_urls")
    private List<String> imageUrls;

    private String status;
}
