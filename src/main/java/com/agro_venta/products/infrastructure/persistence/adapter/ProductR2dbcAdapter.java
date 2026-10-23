package com.agro_venta.products.infrastructure.persistence.adapter;

import com.agro_venta.products.domain.exception.InsufficientStockException;
import com.agro_venta.products.domain.model.Product;
import com.agro_venta.products.domain.port.ProductQueryPort;
import com.agro_venta.products.infrastructure.entity.SellProducEntity;
import com.agro_venta.products.infrastructure.repository.SellProductR2dbRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Repository
public class ProductR2dbcAdapter implements ProductQueryPort {

    private final SellProductR2dbRepository repository;

    public ProductR2dbcAdapter(SellProductR2dbRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<Product> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Product> findCatalog(String category, String destinationCity, int page, int size) {
        long offset = (long) page * size;
        return repository.findCatalog(category, destinationCity, size, offset)
                .map(this::toDomain);
    }

    @Override
    public Flux<Product> findBySellerId(UUID sellerId) {
        return repository.findBySellerId(sellerId).map(this::toDomain);
    }

    @Override
    public Mono<Product> save(Product product) {
        return repository.save(toEntity(product)).map(this::toDomain);
    }

    @Override
    public Mono<Product> reserveStock(UUID productId, BigDecimal quantity) {
        return repository.reserveStock(productId, quantity)
                .flatMap(updated -> {
                    if (updated == 0) {
                        return Mono.error(new InsufficientStockException(
                                "Stock insuficiente para reservar " + quantity));
                    }
                    return repository.findById(productId).map(this::toDomain);
                });
    }

    @Override
    public Mono<Product> releaseStock(UUID productId, BigDecimal quantity) {
        return repository.releaseStock(productId, quantity)
                .then(repository.findById(productId).map(this::toDomain));
    }

    // ─── Mapping ─────────────────────────────────────────────────────────────

    private Product toDomain(SellProducEntity e) {
        return new Product(
                e.getId(), e.getSellerId(),
                e.getName(), e.getDescription(), e.getCategory(),
                e.getQuantity(),
                e.getReservedQuantity() != null ? e.getReservedQuantity() : BigDecimal.ZERO,
                e.getUnit(), e.getUnitPrice(), e.getTotalPrice(),
                e.getMinShipmentQuantity() != null ? e.getMinShipmentQuantity() : BigDecimal.ONE,
                e.getOriginLocation(),
                arrayToList(e.getDestinationCities()),
                arrayToList(e.getImageUrls()),
                e.getStatus(),
                e.getHarvestDate(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private SellProducEntity toEntity(Product p) {
        return SellProducEntity.builder()
                .id(p.id())
                .sellerId(p.sellerId())
                .name(p.name())
                .description(p.description())
                .category(p.category())
                .quantity(p.quantity())
                .reservedQuantity(p.reservedQuantity() != null ? p.reservedQuantity() : BigDecimal.ZERO)
                .unit(p.unit())
                .unitPrice(p.unitPrice())
                .totalPrice(p.totalPrice())
                .minShipmentQuantity(p.minShipmentQuantity())
                .originLocation(p.originLocation())
                .destinationCities(p.destinationCities() != null
                        ? p.destinationCities().toArray(new String[0]) : new String[0])
                .imageUrls(p.imageUrls() != null
                        ? p.imageUrls().toArray(new String[0]) : new String[0])
                .status(p.status())
                .harvestDate(p.harvestDate())
                .build();
    }

    private List<String> arrayToList(String[] arr) {
        if (arr == null) return Collections.emptyList();
        return Arrays.asList(arr);
    }
}
