package com.smartlocal.dto;

import java.time.LocalDateTime;

/**
 * Detailed product catalog entry for Administrator Dashboard overview.
 */
public class AdminProductDetailDto {

    private Long id;
    private String name;
    private String sku;
    private String category;
    private Double price;
    private Double originalPrice;
    private Integer quantity;
    private Integer lowStockThreshold;
    private String unit;
    private String shopName;
    private String locationName;
    private String stockStatus;
    private LocalDateTime createdAt;

    public AdminProductDetailDto() {
    }

    public AdminProductDetailDto(Long id, String name, String sku, String category,
                                Double price, Double originalPrice, Integer quantity,
                                Integer lowStockThreshold, String unit, String shopName,
                                String locationName, String stockStatus, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.originalPrice = originalPrice;
        this.quantity = quantity;
        this.lowStockThreshold = lowStockThreshold;
        this.unit = unit;
        this.shopName = shopName;
        this.locationName = locationName;
        this.stockStatus = stockStatus;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(Double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
