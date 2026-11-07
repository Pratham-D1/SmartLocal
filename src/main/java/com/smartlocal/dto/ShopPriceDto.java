package com.smartlocal.dto;

/**
 * DTO representing an individual shop's price quotation for a product.
 */
public class ShopPriceDto {
    private Long shopId;
    private String shopName;
    private double price;
    private double originalPrice;
    private String stockStatus;
    private int stockQuantity;
    private String demoDistance;
    private String locationName;
    private String shopPhone;
    private boolean isBestPrice;

    public ShopPriceDto() {
    }

    public ShopPriceDto(Long shopId, String shopName, double price, double originalPrice,
                        String stockStatus, int stockQuantity, String demoDistance,
                        String locationName, String shopPhone, boolean isBestPrice) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.price = price;
        this.originalPrice = originalPrice;
        this.stockStatus = stockStatus;
        this.stockQuantity = stockQuantity;
        this.demoDistance = demoDistance;
        this.locationName = locationName;
        this.shopPhone = shopPhone;
        this.isBestPrice = isBestPrice;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getDemoDistance() {
        return demoDistance;
    }

    public void setDemoDistance(String demoDistance) {
        this.demoDistance = demoDistance;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getShopPhone() {
        return shopPhone;
    }

    public void setShopPhone(String shopPhone) {
        this.shopPhone = shopPhone;
    }

    public boolean isBestPrice() {
        return isBestPrice;
    }

    public void setBestPrice(boolean bestPrice) {
        isBestPrice = bestPrice;
    }

    public boolean hasShopPhone() {
        return shopPhone != null && !shopPhone.trim().isEmpty() && !"Not provided".equalsIgnoreCase(shopPhone.trim());
    }
}
