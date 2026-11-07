package com.smartlocal.dto;

import java.util.List;

/**
 * DTO consolidating multi-store price comparisons for a specific product.
 */
public class PriceComparisonDto {
    private Long productId;
    private String productName;
    private String category;
    private double lowestPrice;
    private double highestPrice;
    private double potentialSavings;
    private List<ShopPriceDto> storePrices;

    public PriceComparisonDto() {
    }

    public PriceComparisonDto(Long productId, String productName, String category,
                              double lowestPrice, double highestPrice, double potentialSavings,
                              List<ShopPriceDto> storePrices) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.lowestPrice = lowestPrice;
        this.highestPrice = highestPrice;
        this.potentialSavings = potentialSavings;
        this.storePrices = storePrices;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLowestPrice() {
        return lowestPrice;
    }

    public void setLowestPrice(double lowestPrice) {
        this.lowestPrice = lowestPrice;
    }

    public double getHighestPrice() {
        return highestPrice;
    }

    public void setHighestPrice(double highestPrice) {
        this.highestPrice = highestPrice;
    }

    public double getPotentialSavings() {
        return potentialSavings;
    }

    public void setPotentialSavings(double potentialSavings) {
        this.potentialSavings = potentialSavings;
    }

    public List<ShopPriceDto> getStorePrices() {
        return storePrices;
    }

    public void setStorePrices(List<ShopPriceDto> storePrices) {
        this.storePrices = storePrices;
    }
}
