package com.smartlocal.dto;

/**
 * Data Transfer Object representing a shop preview for discovery.
 * Clearly stores name, address, associated demo location, contact, status, and product count.
 */
public class ShopSummaryDto {
    private Long id;
    private String name;
    private String category;
    private String locationId;
    private String locationName;
    private String address;
    private String demoDistance;
    private double rating;
    private int totalProducts;
    private String contactNumber;
    private String status;
    private String badgeText;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private boolean isGpsCalculated;
    private String formattedStraightLineDistance;
    private String locality;
    private String city = "Pune";

    public ShopSummaryDto() {
    }

    public ShopSummaryDto(Long id, String name, String category, String locationId, String locationName,
                          String address, String demoDistance, double rating, int totalProducts,
                          String contactNumber, String status, String badgeText) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.locationId = locationId;
        this.locationName = locationName;
        this.address = address;
        this.demoDistance = demoDistance;
        this.rating = rating;
        this.totalProducts = totalProducts;
        this.contactNumber = contactNumber;
        this.status = status;
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

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDemoDistance() {
        return demoDistance;
    }

    public void setDemoDistance(String demoDistance) {
        this.demoDistance = demoDistance;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getProductCount() {
        return totalProducts;
    }

    public void setProductCount(int productCount) {
        this.totalProducts = productCount;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public boolean hasContactNumber() {
        return contactNumber != null && !contactNumber.trim().isEmpty() && !"Not provided".equalsIgnoreCase(contactNumber.trim());
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public boolean isGpsCalculated() {
        return isGpsCalculated;
    }

    public void setGpsCalculated(boolean gpsCalculated) {
        isGpsCalculated = gpsCalculated;
    }

    public boolean hasValidCoordinates() {
        return latitude != null && longitude != null
                && !latitude.isNaN() && !longitude.isNaN()
                && latitude >= -90.0 && latitude <= 90.0
                && longitude >= -180.0 && longitude <= 180.0;
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

    public String getLocality() {
        return (locality != null && !locality.isBlank()) ? locality : locationName;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return (city != null && !city.isBlank()) ? city : "Pune";
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getImageUrl() {
        if (category == null) {
            return "https://images.unsplash.com/photo-1513151233558-d860c5398176?auto=format&fit=crop&w=800&q=80";
        }
        String cat = category.toLowerCase();
        if (cat.contains("grocer") || cat.contains("supermarket") || cat.contains("fresh")) {
            return "https://images.unsplash.com/photo-1578916171728-46686eac8d58?auto=format&fit=crop&w=800&q=80";
        } else if (cat.contains("tech") || cat.contains("electro")) {
            return "https://images.unsplash.com/photo-1550009158-9ebf69173e03?auto=format&fit=crop&w=800&q=80";
        } else if (cat.contains("dairy") || cat.contains("bakery")) {
            return "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80";
        } else if (cat.contains("pharm") || cat.contains("health") || cat.contains("care")) {
            return "https://images.unsplash.com/photo-1584308666744-24d5c474f2ae?auto=format&fit=crop&w=800&q=80";
        } else if (cat.contains("cloth") || cat.contains("fashion") || cat.contains("textile") || cat.contains("handloom")) {
            return "https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=800&q=80";
        }
        return "https://images.unsplash.com/photo-1472851294608-062f824d29cc?auto=format&fit=crop&w=800&q=80";
    }
}
