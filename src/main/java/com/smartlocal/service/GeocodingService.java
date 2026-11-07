package com.smartlocal.service;

import com.smartlocal.dto.GeocodingResultDto;
import java.util.List;
import java.util.Optional;

/**
 * Service for address geocoding, manual location search, and reverse geocoding.
 */
public interface GeocodingService {

    /**
     * Search locations and localities by free-form user query.
     */
    List<GeocodingResultDto> searchLocations(String query);

    /**
     * Reverse geocodes customer coordinates to a readable locality/address name.
     */
    Optional<String> reverseGeocode(double latitude, double longitude);

    /**
     * Geocodes a shop address to coordinates if missing.
     */
    Optional<GeocodingResultDto> geocodeAddress(String address);
}

