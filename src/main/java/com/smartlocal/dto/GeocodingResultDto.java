package com.smartlocal.dto;

/**
 * Data Transfer Object for geocoded location search suggestions and reverse geocoding results.
 */
public class GeocodingResultDto {

    private String displayName;
    private String name;
    private String locality;
    private String city;
    private double latitude;
    private double longitude;

    public GeocodingResultDto() {
    }

    public GeocodingResultDto(String displayName, String name, String locality, String city, double latitude, double longitude) {
        this.displayName = displayName;
        this.name = name;
        this.locality = locality;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
