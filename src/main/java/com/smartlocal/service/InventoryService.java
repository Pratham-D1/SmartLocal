package com.smartlocal.service;

import com.smartlocal.dto.InventoryStatsDto;
import com.smartlocal.dto.ProductDto;
import com.smartlocal.entity.Product;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;

import java.util.List;

/**
 * Service for retailer inventory management with backend shop ownership enforcement.
 */
public interface InventoryService {

    /**
     * Add a new product to the retailer's shop inventory.
     */
    Product addProduct(User retailerUser, ProductDto dto);

    /**
     * Update an existing product in the retailer's shop inventory.
     */
    Product updateProduct(User retailerUser, Long productId, ProductDto dto);

    /**
     * Delete a product belonging to the retailer's shop.
     */
    void deleteProduct(User retailerUser, Long productId);

    /**
     * Retrieve a product, ensuring it belongs to the authenticated retailer's shop.
     */
    Product getProductForRetailer(User retailerUser, Long productId);

    /**
     * Retrieve and filter products for the retailer's shop.
     */
    List<Product> getRetailerInventory(User retailerUser, String keyword, String category, String stockFilter);

    /**
     * Calculate summary inventory statistics (in-stock, low-stock, out-of-stock).
     */
    InventoryStatsDto getInventoryStats(User retailerUser);

    /**
     * Quickly update stock quantity and price for a product.
     */
    Product quickUpdate(User retailerUser, Long productId, Integer quantity, Double price);

    /**
     * Seeds initial demonstration inventory if store inventory is empty.
     */
    void initializeDefaultInventoryIfEmpty(Shop defaultShop);
}
