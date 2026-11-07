package com.smartlocal.service;

import com.smartlocal.dto.DemoLocationDto;
import com.smartlocal.dto.ShopSummaryDto;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing predefined demonstration locations and nearby shop discovery.
 */
public interface ShopDiscoveryService {

    /**
     * Retrieve the predefined demo locations (Location A & Location B).
     */
    List<DemoLocationDto> getDemoLocations();

    /**
     * Find a demo location by ID (LOC-PUNE-A or LOC-PUNE-B).
     */
    Optional<DemoLocationDto> getLocationById(String locationId);

    /**
     * Retrieve all verified shops associated with the specified demo location.
     */
    List<ShopSummaryDto> getShopsByLocation(String locationId);

    /**
     * Retrieve all verified shops associated with the specified demo location,
     * calculating real-time GPS distance if user coordinates are provided.
     */
    List<ShopSummaryDto> getShopsByLocationAndCoordinates(String locationId, Double lat, Double lng);

    /**
     * Discover shops sorted by real-time GPS proximity from coordinates.
     */
    List<ShopSummaryDto> getShopsByCoordinates(Double lat, Double lng);

    /**
     * Determine the nearest demo zone ID (LOC-PUNE-A or LOC-PUNE-B) from user GPS coordinates.
     */
    String findNearestLocationId(Double lat, Double lng);

    /**
     * Calculate Great-Circle distance between two coordinates in kilometers using Haversine formula.
     */
    double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2);

    /**
     * Retrieve all approved active shops, calculate actual straight-line Haversine distances
     * from user coordinates, apply optional radius filter (in km), and sort (nearest first).
     */
    List<ShopSummaryDto> getNearbyShops(Double userLat, Double userLng, Double maxRadiusKm, String sortBy);

    /**
     * Retrieve all featured shops across all demonstration zones.
     */
    List<ShopSummaryDto> getFeaturedDemoShops();

    /**
     * Seeds predefined demo locations and shops in the database.
     */
    void initializeDemoLocationsAndShops();
}
