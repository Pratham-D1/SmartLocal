package com.smartlocal.dto;

/**
 * Data Transfer Object representing a predefined demonstration location.
 * Explicitly designated as illustrative demonstration data (not live GPS tracking).
 */
public class DemoLocationDto {
    private String id;
    private String name;
    private String shortLabel;
    private String landmark;
    private String city;
    private String postalCode;
    private String description;
    private String demoCoordinates;
    private Double latitude;
    private Double longitude;

    public DemoLocationDto() {
    }

    public DemoLocationDto(String id, String name, String shortLabel, String landmark,
                           String city, String postalCode, String description, String demoCoordinates) {
        this(id, name, shortLabel, landmark, city, postalCode, description, demoCoordinates, null, null);
    }

    public DemoLocationDto(String id, String name, String shortLabel, String landmark,
                           String city, String postalCode, String description, String demoCoordinates,
                           Double latitude, Double longitude) {
        this.id = id;
        this.name = name;
        this.shortLabel = shortLabel;
        this.landmark = landmark;
        this.city = city;
        this.postalCode = postalCode;
        this.description = description;
        this.demoCoordinates = demoCoordinates;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortLabel() {
        return shortLabel;
    }

    public void setShortLabel(String shortLabel) {
        this.shortLabel = shortLabel;
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = landmark;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDemoCoordinates() {
        return demoCoordinates;
    }

    public void setDemoCoordinates(String demoCoordinates) {
        this.demoCoordinates = demoCoordinates;
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
}
