package com.smartlocal.dto;

/**
 * DTO representing a product result card displayed in the customer dashboard.
 * Connects product details with shop information, stock status, and contact actions.
 */
public class ProductCardDto {
    private Long id;
    private String name;
    private String category;
    private Long shopId;
    private String shopName;
    private String shopContact;
    private double price;
    private double originalPrice;
    private int stockQuantity;
    private boolean inStock;
    private String locationId;
    private String shopAddress;
    private String locationName;
    private String demoDistance;
    private String badgeText;
    private boolean cheapestOption;
    private String unit = "pcs";
    private Double distanceKm;
    private String formattedStraightLineDistance;
    private Double shopLat;
    private Double shopLng;

    public ProductCardDto() {
    }

    public ProductCardDto(Long id, String name, String category, Long shopId, String shopName,
                          String shopContact, double price, double originalPrice,
                          int stockQuantity, boolean inStock, String locationName,
                          String demoDistance, String badgeText) {
        this(id, name, category, shopId, shopName, shopContact, "", price, originalPrice, stockQuantity, inStock, "LOC-PUNE-A", locationName, demoDistance, badgeText);
    }

    public ProductCardDto(Long id, String name, String category, Long shopId, String shopName,
                          String shopContact, String shopAddress, double price, double originalPrice,
                          int stockQuantity, boolean inStock, String locationId, String locationName,
                          String demoDistance, String badgeText) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.shopId = shopId;
        this.shopName = shopName;
        this.shopContact = shopContact;
        this.shopAddress = shopAddress;
        this.price = price;
        this.originalPrice = originalPrice;
        this.stockQuantity = stockQuantity;
        this.inStock = inStock;
        this.locationId = locationId;
        this.locationName = locationName;
        this.demoDistance = demoDistance;
        this.badgeText = badgeText;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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

    public String getShopContact() {
        return shopContact;
    }

    public void setShopContact(String shopContact) {
        this.shopContact = shopContact;
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

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDemoDistance() {
        return demoDistance;
    }

    public void setDemoDistance(String demoDistance) {
        this.demoDistance = demoDistance;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public boolean isCheapestOption() {
        return cheapestOption;
    }

    public void setCheapestOption(boolean cheapestOption) {
        this.cheapestOption = cheapestOption;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean hasShopContact() {
        return shopContact != null && !shopContact.trim().isEmpty() && !"Not provided".equalsIgnoreCase(shopContact.trim());
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public String getFormattedStraightLineDistance() {
        if (formattedStraightLineDistance != null && !formattedStraightLineDistance.isBlank()) {
            return formattedStraightLineDistance;
        }
        if (distanceKm == null) {
            return "Distance unavailable";
        }
        if (distanceKm < 1.0) {
            long meters = Math.round(distanceKm * 1000.0);
            return "Approx. " + meters + " m straight-line distance";
        } else {
            return String.format(java.util.Locale.US, "Approx. %.1f km straight-line distance", distanceKm);
        }
    }

    public void setFormattedStraightLineDistance(String formattedStraightLineDistance) {
        this.formattedStraightLineDistance = formattedStraightLineDistance;
    }

    public String getDistanceBadgeText() {
        if (distanceKm == null) {
            return "Distance unavailable";
        }
        if (distanceKm < 1.0) {
            long meters = Math.round(distanceKm * 1000.0);
            return meters + " m away";
        } else {
            return String.format(java.util.Locale.US, "%.1f km away", distanceKm);
        }
    }

    public Double getShopLat() {
        return shopLat;
    }

    public void setShopLat(Double shopLat) {
        this.shopLat = shopLat;
    }

    public Double getShopLng() {
        return shopLng;
    }

    public void setShopLng(Double shopLng) {
        this.shopLng = shopLng;
    }

    public String getImageUrl() {
        if (name == null && category == null) {
            return "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=800&q=80";
        }
        String n = (name != null ? name : "").toLowerCase();
        String cat = (category != null ? category : "").toLowerCase();

        // 1. Specific product name matches
        if (n.contains("egg") || n.contains("anda") || n.contains("ande") || n.contains("poultry")) {
            return "https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("mouse") || n.contains("optical")) {
            return "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("cable") || n.contains("usb") || n.contains("charger") || n.contains("braided")) {
            return "https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("mango") || n.contains("aam") || n.contains("pulp")) {
            return "https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("paracetamol") || n.contains("tablet") || cat.contains("pharm") || cat.contains("health")) {
            return "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("oil") || n.contains("mustard") || n.contains("olive")) {
            return "https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("rice") || n.contains("basmati")) {
            return "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("atta") || n.contains("wheat") || n.contains("flour")) {
            return "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("milk") || n.contains("ghee") || n.contains("paneer") || n.contains("yogurt") || cat.contains("dairy")) {
            return "https://images.unsplash.com/photo-1550583724-b2692b85b150?auto=format&fit=crop&w=800&q=80";
        } else if (n.contains("kurta") || n.contains("shirt") || cat.contains("cloth") || cat.contains("fashion") || cat.contains("textile") || cat.contains("handloom")) {
            return "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=800&q=80";
        } else if (cat.contains("electro") || cat.contains("tech")) {
            return "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=800&q=80";
        } else if (cat.contains("grocer")) {
            return "https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=800&q=80";
        }
        return "https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=800&q=80";
    }
}
