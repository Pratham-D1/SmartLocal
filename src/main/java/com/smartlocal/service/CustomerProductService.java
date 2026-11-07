package com.smartlocal.service;

import com.smartlocal.dto.PriceComparisonDto;
import com.smartlocal.dto.ProductCardDto;
import com.smartlocal.dto.ProductCategoryDto;

import java.util.List;
import java.util.Optional;

/**
 * Service for customer product search, category exploration, and price comparison previews.
 */
public interface CustomerProductService {

    /**
     * Retrieves product categories with icon metadata and item counts.
     */
    List<ProductCategoryDto> getCategories();

    /**
     * Searches products filtered by optional keyword, category, and location ID.
     */
    List<ProductCardDto> searchProducts(String keyword, String category, String locationId);

    /**
     * Searches products with keyword, category, location ID, and price sorting options.
     * Highlights cheapest available option while excluding out-of-stock items.
     */
    List<ProductCardDto> searchProducts(String keyword, String category, String locationId, String sortBy);

    /**
     * Searches products with keyword, category, location, coordinates, radius filter, and sorting criteria.
     * Computes actual straight-line distance to each product's shop and supports sorting by nearest or lowest price.
     */
    List<ProductCardDto> searchProducts(String keyword, String category, String locationId, Double userLat, Double userLng, Double maxRadiusKm, String sortBy);

    /**
     * Retrieves price comparison breakdown across multiple nearby local shops for a product.
     */
    Optional<PriceComparisonDto> getPriceComparisonForProduct(Long productId);

    /**
     * Retrieves a single product by ID.
     */
    Optional<ProductCardDto> getProductById(Long productId);
}
