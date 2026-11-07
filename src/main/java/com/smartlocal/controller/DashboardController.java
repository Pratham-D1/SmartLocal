package com.smartlocal.controller;

import com.smartlocal.dto.GeocodingResultDto;
import com.smartlocal.dto.PriceComparisonDto;
import com.smartlocal.dto.ProductCardDto;
import com.smartlocal.dto.ShopSummaryDto;
import com.smartlocal.entity.User;
import com.smartlocal.service.CustomerProductService;
import com.smartlocal.service.GeocodingService;
import com.smartlocal.service.SearchSynonymService;
import com.smartlocal.service.ShopDiscoveryService;
import com.smartlocal.service.UserService;
import com.smartlocal.service.impl.ShopDiscoveryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller serving authenticated user dashboards.
 * Module 3: Fully connects the Customer Dashboard with profile details,
 * location selector, category filters, product search, nearby stores,
 * price comparison navigation, and contact retailer interactions.
 */
@Controller
public class DashboardController {

    private final UserService userService;
    private final ShopDiscoveryService shopDiscoveryService;
    private final CustomerProductService customerProductService;
    private final SearchSynonymService searchSynonymService;
    private final GeocodingService geocodingService;

    @Autowired
    public DashboardController(UserService userService,
                               ShopDiscoveryService shopDiscoveryService,
                               CustomerProductService customerProductService,
                               SearchSynonymService searchSynonymService,
                               GeocodingService geocodingService) {
        this.userService = userService;
        this.shopDiscoveryService = shopDiscoveryService;
        this.customerProductService = customerProductService;
        this.searchSynonymService = searchSynonymService;
        this.geocodingService = geocodingService;
    }

    /**
     * Customer Dashboard with search, category filtering, location selection,
     * nearby stores, real-time GPS geolocation, and product availability results.
     */
    @GetMapping("/customer/dashboard")
    public String customerDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false, defaultValue = "all") String category,
            @RequestParam(value = "locationId", required = false) String locationId,
            @RequestParam(value = "lat", required = false) Double lat,
            @RequestParam(value = "lng", required = false) Double lng,
            @RequestParam(value = "locationName", required = false) String locationName,
            @RequestParam(value = "radius", required = false) Double radius,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "compareId", required = false) Long compareId,
            Model model) {

        populateUserModel(userDetails, model);

        boolean hasUserCoords = ShopDiscoveryServiceImpl.isValidCoordinate(lat, lng);
        String resolvedLocationName = (locationName != null && !locationName.trim().isEmpty()) ? locationName.trim() : null;

        // If coordinates are provided without an explicit location name, reverse-geocode to get locality
        if (hasUserCoords && (resolvedLocationName == null || resolvedLocationName.isBlank())) {
            Optional<String> rev = geocodingService.reverseGeocode(lat, lng);
            resolvedLocationName = rev.orElse("Detected Location (" + String.format(java.util.Locale.US, "%.4f, %.4f", lat, lng) + ")");
        }

        // Determine active sorting: default to nearest when customer coordinates exist, otherwise price_asc
        String activeSortBy;
        if (sortBy != null && !sortBy.trim().isEmpty()) {
            activeSortBy = sortBy.trim();
        } else {
            activeSortBy = hasUserCoords ? "nearest" : "price_asc";
        }

        String activeLocation;
        List<ShopSummaryDto> nearbyShops;
        List<ProductCardDto> products;

        if (hasUserCoords) {
            // Real location origin: Calculate Haversine straight-line distances and sort/filter by radius
            activeLocation = (locationId != null && !locationId.trim().isEmpty()) ? locationId.trim() : "ALL";
            nearbyShops = shopDiscoveryService.getNearbyShops(lat, lng, radius, activeSortBy);
            products = customerProductService.searchProducts(query, category, locationId, lat, lng, radius, activeSortBy);
        } else {
            // Predefined zone selection
            activeLocation = (locationId != null && !locationId.trim().isEmpty()) ? locationId.trim() : "LOC-PUNE-A";
            if ("LOC-DEMO-1".equalsIgnoreCase(activeLocation)) {
                activeLocation = "LOC-PUNE-A";
            } else if ("LOC-DEMO-2".equalsIgnoreCase(activeLocation)) {
                activeLocation = "LOC-PUNE-B";
            }

            if ("ALL".equalsIgnoreCase(activeLocation)) {
                nearbyShops = shopDiscoveryService.getNearbyShops(null, null, null, activeSortBy);
                products = customerProductService.searchProducts(query, category, "ALL", null, null, null, activeSortBy);
            } else {
                nearbyShops = shopDiscoveryService.getShopsByLocation(activeLocation);
                products = customerProductService.searchProducts(query, category, activeLocation, null, null, null, activeSortBy);
            }
        }

        // Pass attributes to model
        model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
        model.addAttribute("selectedLocationId", activeLocation);
        model.addAttribute("userLat", hasUserCoords ? lat : null);
        model.addAttribute("userLng", hasUserCoords ? lng : null);
        model.addAttribute("userLocationName", resolvedLocationName);
        model.addAttribute("isLocationDetected", hasUserCoords);
        model.addAttribute("isGpsActive", hasUserCoords);
        model.addAttribute("selectedRadius", (radius != null && radius > 0) ? radius : 0.0);
        model.addAttribute("selectedSortBy", activeSortBy);

        shopDiscoveryService.getLocationById(activeLocation).ifPresent(loc -> model.addAttribute("currentLocation", loc));

        // Categories & search parameters
        model.addAttribute("categories", customerProductService.getCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("searchQuery", query != null ? query.trim() : "");

        // Rule-based synonym explanation for AI-assisted search transparency
        if (query != null && !query.trim().isEmpty()) {
            searchSynonymService.getSynonymExplanation(query).ifPresent(exp -> model.addAttribute("synonymExplanation", exp));
        }

        // Product search results
        model.addAttribute("products", products);
        model.addAttribute("totalProductsFound", products.size());

        // Nearby shops
        model.addAttribute("nearbyShops", nearbyShops);
        model.addAttribute("totalShopsFound", nearbyShops.size());

        // Optional price comparison highlight if compareId is provided
        if (compareId != null) {
            Optional<PriceComparisonDto> comparisonOpt = customerProductService.getPriceComparisonForProduct(compareId);
            comparisonOpt.ifPresent(comp -> model.addAttribute("activeComparison", comp));
        }

        return "dashboard/customer-dashboard";
    }

    /**
     * REST API for searching real locations (landmarks, localities, streets) via geocoding provider.
     */
    @GetMapping("/api/location/search")
    @ResponseBody
    public ResponseEntity<List<GeocodingResultDto>> searchLocations(
            @RequestParam("query") String query) {
        if (query == null || query.trim().length() < 2) {
            return ResponseEntity.ok(List.of());
        }
        List<GeocodingResultDto> results = geocodingService.searchLocations(query.trim());
        return ResponseEntity.ok(results);
    }

    /**
     * REST API for reverse geocoding coordinates to a readable locality/address.
     */
    @GetMapping("/api/location/reverse")
    @ResponseBody
    public ResponseEntity<?> reverseGeocode(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng) {
        if (!ShopDiscoveryServiceImpl.isValidCoordinate(lat, lng)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid coordinates"));
        }
        Optional<String> addressOpt = geocodingService.reverseGeocode(lat, lng);
        Map<String, Object> resp = new HashMap<>();
        resp.put("latitude", lat);
        resp.put("longitude", lng);
        resp.put("displayName", addressOpt.orElse("Pune, Maharashtra, India"));
        return ResponseEntity.ok(resp);
    }

    /**
     * REST API for client-side Real-Time Geolocation proximity lookups.
     */
    @GetMapping("/api/location/nearest")
    @ResponseBody
    public ResponseEntity<?> getNearestLocation(
            @RequestParam("lat") Double lat,
            @RequestParam("lng") Double lng) {

        String nearestId = shopDiscoveryService.findNearestLocationId(lat, lng);
        Optional<com.smartlocal.dto.DemoLocationDto> locOpt = shopDiscoveryService.getLocationById(nearestId);
        List<ShopSummaryDto> shops = shopDiscoveryService.getShopsByLocationAndCoordinates(nearestId, lat, lng);

        Map<String, Object> resp = new HashMap<>();
        resp.put("userLat", lat);
        resp.put("userLng", lng);
        resp.put("nearestLocationId", nearestId);
        locOpt.ifPresent(l -> {
            resp.put("locationName", l.getName());
            resp.put("landmark", l.getLandmark());
            resp.put("distanceToCenterKm", shopDiscoveryService.calculateDistanceKm(lat, lng, l.getLatitude(), l.getLongitude()));
        });
        resp.put("shops", shops);

        return ResponseEntity.ok(resp);
    }

    /**
     * Dedicated Price Comparison View.
     */
    @GetMapping("/customer/compare")
    public String compareProductPrices(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("productId") Long productId,
            Model model) {

        populateUserModel(userDetails, model);

        Optional<PriceComparisonDto> comparisonOpt = customerProductService.getPriceComparisonForProduct(productId);
        if (comparisonOpt.isPresent()) {
            model.addAttribute("comparison", comparisonOpt.get());
            return "dashboard/price-comparison";
        }

        return "redirect:/customer/dashboard";
    }





    private void populateUserModel(UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
    }
}
