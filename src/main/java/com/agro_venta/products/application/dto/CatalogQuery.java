package com.agro_venta.products.application.dto;

public record CatalogQuery(String category, String destinationCity, String search, int page, int size) {
}
