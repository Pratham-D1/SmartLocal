package com.smartlocal.service.impl;

import com.smartlocal.dto.DemoLocationDto;
import com.smartlocal.dto.ShopSummaryDto;
import com.smartlocal.entity.ApprovalStatus;
import com.smartlocal.entity.Product;
import com.smartlocal.entity.Role;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;
import com.smartlocal.repository.ProductRepository;
import com.smartlocal.repository.ShopRepository;
import com.smartlocal.repository.UserRepository;
import com.smartlocal.service.ShopDiscoveryService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service managing demonstration locations (Location A & Location B)
 * and location-based shop discovery with real database persistence.
 */
@Service
public class ShopDiscoveryServiceImpl implements ShopDiscoveryService {

    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final List<DemoLocationDto> demoLocations = new ArrayList<>();

    @Autowired
    public ShopDiscoveryServiceImpl(ShopRepository shopRepository,
                                   ProductRepository productRepository,
                                   UserRepository userRepository,
                                   PasswordEncoder passwordEncoder,
                                   com.smartlocal.service.GeocodingService geocodingService) {
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        initLocations();
    }

    private void initLocations() {
        demoLocations.add(new DemoLocationDto(
                "LOC-PUNE-A",
                "Central Pune (F.C. Road / Deccan)",
                "Central Pune",
                "Near F.C. Road & J.M. Road Commercial Arcade",
                "Pune",
                "411004",
                "High-density commercial and retail zone covering F.C. Road, J.M. Road, and Deccan Gymkhana.",
                "18.5204° N, 73.8567° E",
                18.5204,
                73.8567
        ));

        demoLocations.add(new DemoLocationDto(
                "LOC-PUNE-B",
                "East Pune (Viman Nagar / Nagar Road)",
                "East Pune",
                "Opposite Viman Nagar Town Plaza & Nagar Road",
                "Pune",
                "411014",
                "Major commercial, residential and tech district covering Viman Nagar, Nagar Road, and Kalyani Nagar.",
                "18.5679° N, 73.9143° E",
                18.5679,
                73.9143
        ));

        demoLocations.add(new DemoLocationDto(
                "ALL",
                "All Pune (Central & East)",
                "All Pune",
                "All Available Neighborhoods & Zones",
                "Pune",
                "411001",
                "Metropolitan directory of all verified local shops across Central and East Pune.",
                "18.5204° N, 73.8567° E",
                18.5204,
                73.8567
        ));
    }

    @Override
    public List<DemoLocationDto> getDemoLocations() {
        return demoLocations;
    }

    @Override
    public Optional<DemoLocationDto> getLocationById(String locationId) {
        if (locationId == null || locationId.trim().isEmpty()) {
            return Optional.empty();
        }
        return demoLocations.stream()
                .filter(loc -> loc.getId().equalsIgnoreCase(locationId.trim()))
                .findFirst();
    }

    @Override
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's mean radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((R * c) * 100.0) / 100.0;
    }

    @Override
    public String findNearestLocationId(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return "LOC-PUNE-A";
        }
        double distA = calculateDistanceKm(lat, lng, 18.5204, 73.8567);
        double distB = calculateDistanceKm(lat, lng, 18.5679, 73.9143);
        return distA <= distB ? "LOC-PUNE-A" : "LOC-PUNE-B";
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopSummaryDto> getShopsByLocation(String locationId) {
        return getShopsByLocationAndCoordinates(locationId, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopSummaryDto> getNearbyShops(Double userLat, Double userLng, Double maxRadiusKm, String sortBy) {
        boolean hasValidUserCoords = isValidCoordinate(userLat, userLng);

        List<Shop> allApprovedShops = shopRepository.findAll().stream()
                .filter(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED && s.isActive())
                .collect(Collectors.toList());

        List<ShopSummaryDto> dtoList = new ArrayList<>();

        for (Shop s : allApprovedShops) {
            long productCount = productRepository.countByShop(s);
            Double shopLat = s.getLatitude();
            Double shopLng = s.getLongitude();
            boolean hasShopCoords = isValidCoordinate(shopLat, shopLng);

            Double distanceKm = null;
            String shortDist;
            String straightLineDist;

            if (hasValidUserCoords && hasShopCoords) {
                distanceKm = calculateDistanceKm(userLat, userLng, shopLat, shopLng);
                if (distanceKm < 1.0) {
                    long meters = Math.round(distanceKm * 1000.0);
                    shortDist = meters + " m away";
                    straightLineDist = "Approx. " + meters + " m straight-line distance";
                } else {
                    shortDist = String.format(java.util.Locale.US, "%.1f km away", distanceKm);
                    straightLineDist = String.format(java.util.Locale.US, "Approx. %.1f km straight-line distance", distanceKm);
                }
            } else if (!hasShopCoords) {
                shortDist = "Distance unavailable";
                straightLineDist = "Coordinates unavailable for this store";
            } else {
                shortDist = "Location not set";
                straightLineDist = "Select your location to calculate straight-line distance";
            }

            ShopSummaryDto dto = new ShopSummaryDto(
                    s.getId(),
                    s.getShopName(),
                    s.getCategory(),
                    s.getLocationId(),
                    s.getLocationName(),
                    s.getAddress(),
                    shortDist,
                    4.8,
                    (int) productCount,
                    s.getContactNumber(),
                    s.getApprovalStatus().getDisplayName(),
                    "Verified Partner"
            );
            dto.setLatitude(shopLat);
            dto.setLongitude(shopLng);
            dto.setDistanceKm(distanceKm);
            dto.setGpsCalculated(hasValidUserCoords && hasShopCoords);
            dto.setFormattedStraightLineDistance(straightLineDist);
            dto.setLocality(s.getLocality());
            dto.setCity(s.getCity());

            // Optional radius filter in kilometers
            if (maxRadiusKm != null && maxRadiusKm > 0) {
                if (distanceKm != null && distanceKm <= maxRadiusKm) {
                    dtoList.add(dto);
                }
            } else {
                dtoList.add(dto);
            }
        }

        // Sorting: nearest first by default when customer coordinates are available
        String activeSort = (sortBy != null && !sortBy.isBlank()) ? sortBy.trim().toLowerCase() : "distance_asc";
        if ("distance_asc".equals(activeSort) || "nearest".equals(activeSort)) {
            dtoList.sort(Comparator.comparing(dto -> dto.getDistanceKm() != null ? dto.getDistanceKm() : 999999.0));
        } else if ("name_asc".equals(activeSort) || "name".equals(activeSort)) {
            dtoList.sort(Comparator.comparing(dto -> dto.getName().toLowerCase()));
        } else if ("rating_desc".equals(activeSort)) {
            dtoList.sort(Comparator.comparingDouble(ShopSummaryDto::getRating).reversed());
        }

        return dtoList;
    }

    public static boolean isValidCoordinate(Double lat, Double lng) {
        return lat != null && lng != null
                && !lat.isNaN() && !lng.isNaN()
                && lat >= -90.0 && lat <= 90.0
                && lng >= -180.0 && lng <= 180.0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopSummaryDto> getShopsByLocationAndCoordinates(String locationId, Double lat, Double lng) {
        // If user coordinates are provided, route to real Haversine distance-based discovery
        if (isValidCoordinate(lat, lng)) {
            return getNearbyShops(lat, lng, null, "distance_asc");
        }

        String targetLoc = (locationId != null && !locationId.trim().isEmpty()) ? locationId.trim() : "LOC-PUNE-A";

        // Query database shops (supports ALL locations or specific neighborhood)
        List<Shop> shops = shopRepository.findAll().stream()
                .filter(s -> "ALL".equalsIgnoreCase(targetLoc) || matchesLocation(s.getLocationId(), targetLoc))
                .filter(s -> s.getApprovalStatus() == ApprovalStatus.APPROVED && s.isActive())
                .collect(Collectors.toList());

        List<ShopSummaryDto> dtoList = new ArrayList<>();
        for (Shop s : shops) {
            long productCount = productRepository.countByShop(s);
            Double shopLat = s.getLatitude();
            Double shopLng = s.getLongitude();

            ShopSummaryDto dto = new ShopSummaryDto(
                    s.getId(),
                    s.getShopName(),
                    s.getCategory(),
                    s.getLocationId(),
                    s.getLocationName(),
                    s.getAddress(),
                    "Location not set",
                    4.8,
                    (int) productCount,
                    s.getContactNumber(),
                    s.getApprovalStatus().getDisplayName(),
                    "Verified Partner"
            );
            dto.setLatitude(shopLat);
            dto.setLongitude(shopLng);
            dto.setDistanceKm(null);
            dto.setGpsCalculated(false);
            dto.setFormattedStraightLineDistance("Select or detect your location to calculate straight-line distance");
            dto.setLocality(s.getLocality());
            dto.setCity(s.getCity());
            dtoList.add(dto);
        }

        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopSummaryDto> getShopsByCoordinates(Double lat, Double lng) {
        String nearestLoc = findNearestLocationId(lat, lng);
        return getShopsByLocationAndCoordinates(nearestLoc, lat, lng);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShopSummaryDto> getFeaturedDemoShops() {
        List<Shop> allShops = shopRepository.findAll();
        List<ShopSummaryDto> dtoList = new ArrayList<>();
        for (Shop s : allShops) {
            long productCount = productRepository.countByShop(s);
            Double shopLat = s.getLatitude() != null ? s.getLatitude() : ("LOC-PUNE-A".equalsIgnoreCase(s.getLocationId()) ? 18.5204 : 18.5679);
            Double shopLng = s.getLongitude() != null ? s.getLongitude() : ("LOC-PUNE-A".equalsIgnoreCase(s.getLocationId()) ? 73.8567 : 73.9143);
            String dist = "LOC-PUNE-A".equalsIgnoreCase(s.getLocationId()) ? "0.4 km away" : "0.6 km away";
            ShopSummaryDto dto = new ShopSummaryDto(
                    s.getId(),
                    s.getShopName(),
                    s.getCategory(),
                    s.getLocationId(),
                    s.getLocationName(),
                    s.getAddress(),
                    dist,
                    4.8,
                    (int) productCount,
                    s.getContactNumber(),
                    s.getApprovalStatus().getDisplayName(),
                    "Verified Partner"
            );
            dto.setLatitude(shopLat);
            dto.setLongitude(shopLng);
            dtoList.add(dto);
        }
        return dtoList;
    }

    @Override
    @PostConstruct
    @Transactional
    public void initializeDemoLocationsAndShops() {
        // =========================================================================
        // 1. Fresh Mart (Suggested Shop 1 - Location A: Central Pune)
        // =========================================================================
        User retailerA1 = seedUserIfMissing("Vikram Malhotra", "retailer@smartlocal.com", "Retailer@123", "+91 98765 43210", Role.ROLE_RETAILER);
        if (retailerA1 != null) {
            Shop shop1 = shopRepository.findByOwner(retailerA1).orElseGet(() -> {
                Shop s = new Shop(
                        "Fresh Mart",
                        retailerA1.getFullName(),
                        "+91 98765 43210",
                        "Shop #10, F.C. Road Arcade, Central Pune",
                        "Groceries & Daily Essentials",
                        "LOC-PUNE-A",
                        "Central Pune",
                        "Premier neighborhood store for fresh staples, flours, pulses, and kitchen daily essentials.",
                        "08:30 AM - 09:30 PM",
                        ApprovalStatus.APPROVED,
                        retailerA1
                );
                return shopRepository.save(s);
            });
            shop1.setShopName("Fresh Mart");
            shop1.setLocationId("LOC-PUNE-A");
            shop1.setLocationName("Central Pune");
            shop1.setAddress("Shop #10, F.C. Road Arcade, Central Pune");
            shop1.setCategory("Groceries & Daily Essentials");
            shop1.setLatitude(18.5204);
            shop1.setLongitude(73.8567);
            shop1.setActive(true);
            shop1.setApprovalStatus(ApprovalStatus.APPROVED);
            shopRepository.save(shop1);

            // Rich sample inventory: in-stock, low-stock, and cross-store pricing
            seedProductIfMissing(shop1, "Aashirvaad Superior Sharbati Whole Wheat Atta (5 kg)", "GROC-ATTA-01", "Groceries & Daily Essentials", 265.00, 310.00, 24, 5, "pack");
            seedProductIfMissing(shop1, "Fortune Everyday Basmati Rice (5 kg)", "GROC-RICE-01", "Groceries & Daily Essentials", 399.00, 499.00, 20, 5, "pack");
            seedProductIfMissing(shop1, "Cold Pressed Pure Mustard Oil (1L)", "GROC-OIL-01", "Groceries & Daily Essentials", 175.00, 210.00, 18, 5, "bottle");
            seedProductIfMissing(shop1, "Fast-Charging USB-C Braided Cable (2m)", "TECH-CAB-01", "Consumer Electronics & Household Essentials", 269.00, 499.00, 26, 8, "pcs");
            seedProductIfMissing(shop1, "Wireless Ergonomic Optical Mouse", "TECH-MOU-01", "Consumer Electronics & Household Essentials", 849.00, 1199.00, 14, 5, "pcs");
            seedProductIfMissing(shop1, "Organic Farm Fresh Brown Eggs (Pack of 12)", "GROC-EGG-01", "Groceries & Daily Essentials", 125.00, 130.00, 3, 5, "pack"); // Low-stock
            seedProductIfMissing(shop1, "Alphonso Mango Real Fruit Pulp Tin (850g)", "GROC-MAN-01", "Groceries & Daily Essentials", 220.00, 260.00, 2, 5, "can"); // Low-stock
        }

        // =========================================================================
        // 2. Daily Needs Store (Suggested Shop 2 - Location A: Central Pune)
        // =========================================================================
        // 2. Daily Needs Store (Suggested Shop 2 - Location A: Central Pune)
        // =========================================================================
        User retailerA2 = seedUserIfMissing("Sunita Agarwal", "dailyneeds@smartlocal.com", "Retailer@123", "+91 98765 43211", Role.ROLE_RETAILER);
        if (retailerA2 != null) {
            Shop shop2 = shopRepository.findByOwner(retailerA2).orElseGet(() -> {
                Shop s = new Shop(
                        "Daily Needs Store",
                        retailerA2.getFullName(),
                        "+91 98765 43211",
                        "Plot #18, J.M. Road Market, Central Pune",
                        "Groceries & Daily Essentials",
                        "LOC-PUNE-A",
                        "Central Pune",
                        "Convenient neighborhood general market providing household staples and branded packaged goods.",
                        "08:00 AM - 10:00 PM",
                        ApprovalStatus.APPROVED,
                        retailerA2
                );
                return shopRepository.save(s);
            });
            shop2.setShopName("Daily Needs Store");
            shop2.setLocationId("LOC-PUNE-A");
            shop2.setLocationName("Central Pune");
            shop2.setAddress("Plot #18, J.M. Road Market, Central Pune");
            shop2.setCategory("Groceries & Daily Essentials");
            shop2.setLatitude(18.5236);
            shop2.setLongitude(73.8519);
            shop2.setActive(true);
            shop2.setApprovalStatus(ApprovalStatus.APPROVED);
            shopRepository.save(shop2);

            // Cross-store price comparisons: Lower price for Atta (₹245) & Mustard Oil (₹169)
            seedProductIfMissing(shop2, "Aashirvaad Superior Sharbati Whole Wheat Atta (5 kg)", "GROC-ATTA-02", "Groceries & Daily Essentials", 245.00, 310.00, 35, 5, "pack");
            seedProductIfMissing(shop2, "Fortune Everyday Basmati Rice (5 kg)", "GROC-RICE-02", "Groceries & Daily Essentials", 425.00, 499.00, 15, 5, "pack");
            seedProductIfMissing(shop2, "Cold Pressed Pure Mustard Oil (1L)", "GROC-OIL-02", "Groceries & Daily Essentials", 169.00, 210.00, 22, 5, "bottle");
            seedProductIfMissing(shop2, "Fast-Charging USB-C Braided Cable (2m)", "TECH-CAB-02", "Consumer Electronics & Household Essentials", 289.00, 499.00, 14, 5, "pcs");
            seedProductIfMissing(shop2, "Wireless Ergonomic Optical Mouse", "TECH-MOU-02", "Consumer Electronics & Household Essentials", 899.00, 1199.00, 4, 5, "pcs"); // Low-stock
        }

        // =========================================================================
        // 3. Green Basket (Suggested Shop 3 - Location A: Central Pune)
        // =========================================================================
        User retailerA3 = seedUserIfMissing("Rajesh Kulkarni", "greenbasket@smartlocal.com", "Retailer@123", "+91 98765 43212", Role.ROLE_RETAILER);
        if (retailerA3 != null) {
            Shop shop3 = shopRepository.findByOwner(retailerA3).orElseGet(() -> {
                Shop s = new Shop(
                        "Green Basket",
                        retailerA3.getFullName(),
                        "+91 98765 43212",
                        "Shop #5, Deccan Gymkhana Avenue, Central Pune",
                        "Organic Fruits, Vegetables & Staples",
                        "LOC-PUNE-A",
                        "Central Pune",
                        "Certified organic store delivering naturally grown cereals, cold-pressed oils, and farm fresh goods.",
                        "08:00 AM - 09:00 PM",
                        ApprovalStatus.APPROVED,
                        retailerA3
                );
                return shopRepository.save(s);
            });
            shop3.setShopName("Green Basket");
            shop3.setLocationId("LOC-PUNE-A");
            shop3.setLocationName("Central Pune");
            shop3.setAddress("Shop #5, Deccan Gymkhana Avenue, Central Pune");
            shop3.setCategory("Organic Fruits, Vegetables & Staples");
            shop3.setLatitude(18.5175);
            shop3.setLongitude(73.8415);
            shop3.setActive(true);
            shop3.setApprovalStatus(ApprovalStatus.APPROVED);
            shopRepository.save(shop3);

            // Cross-store price comparisons, out-of-stock items, and lowest price for Organic Eggs (₹115)
            seedProductIfMissing(shop3, "Aashirvaad Superior Sharbati Whole Wheat Atta (5 kg)", "GROC-ATTA-03", "Groceries & Daily Essentials", 279.00, 310.00, 12, 5, "pack");
            seedProductIfMissing(shop3, "Fortune Everyday Basmati Rice (5 kg)", "GROC-RICE-03", "Groceries & Daily Essentials", 380.00, 499.00, 0, 5, "pack"); // Out-of-stock
            seedProductIfMissing(shop3, "Organic Farm Fresh Brown Eggs (Pack of 12)", "GROC-EGG-03", "Organic Fruits, Vegetables & Staples", 115.00, 130.00, 25, 5, "pack"); // Lowest Price
            seedProductIfMissing(shop3, "Alphonso Mango Real Fruit Pulp Tin (850g)", "GROC-MAN-03", "Organic Fruits, Vegetables & Staples", 240.00, 260.00, 0, 5, "can"); // Out-of-stock
            seedProductIfMissing(shop3, "Cold Pressed Extra Virgin Olive Oil (500ml)", "GROC-OLV-03", "Organic Fruits, Vegetables & Staples", 340.00, 399.00, 3, 5, "bottle"); // Low-stock
        }

        // =========================================================================
        // 4. City Dairy (Suggested Shop 4 - Location B: East Pune)
        // =========================================================================
        User retailerB1 = seedUserIfMissing("Mahesh Patil", "citydairy@smartlocal.com", "Retailer@123", "+91 98765 43213", Role.ROLE_RETAILER);
        if (retailerB1 != null) {
            Shop shop4 = shopRepository.findByOwner(retailerB1).orElseGet(() -> {
                Shop s = new Shop(
                        "City Dairy",
                        retailerB1.getFullName(),
                        "+91 98765 43213",
                        "Shop #3, Nagar Road Plaza, East Pune",
                        "Dairy & Fresh Beverages",
                        "LOC-PUNE-B",
                        "East Pune",
                        "Specialty dairy boutique offering pasteurized toned milk, pure buffalo desi ghee, and fresh paneer daily.",
                        "06:30 AM - 09:30 PM",
                        ApprovalStatus.APPROVED,
                        retailerB1
                );
                return shopRepository.save(s);
            });
            shop4.setShopName("City Dairy");
            shop4.setLocationId("LOC-PUNE-B");
            shop4.setLocationName("East Pune");
            shop4.setAddress("Shop #3, Nagar Road Plaza, East Pune");
            shop4.setCategory("Dairy & Fresh Beverages");
            shop4.setLatitude(18.5580);
            shop4.setLongitude(73.9140);
            shop4.setActive(true);
            shop4.setApprovalStatus(ApprovalStatus.APPROVED);
            shopRepository.save(shop4);

            // Dairy products with lowest prices for Milk (₹72) & Ghee (₹620)
            seedProductIfMissing(shop4, "Amul Taaza Homogenised Toned Milk (1L)", "DAIRY-MLK-01", "Dairy & Fresh Beverages", 72.00, 75.00, 45, 10, "carton");
            seedProductIfMissing(shop4, "Pure Buffalo Desi Ghee (1L)", "DAIRY-GHEE-01", "Dairy & Fresh Beverages", 620.00, 680.00, 28, 5, "jar");
            seedProductIfMissing(shop4, "Fresh Malai Paneer (500g)", "DAIRY-PAN-01", "Dairy & Fresh Beverages", 195.00, 220.00, 20, 5, "pack");
            seedProductIfMissing(shop4, "Traditional Filter Coffee Powder (500g)", "DAIRY-COF-01", "Dairy & Fresh Beverages", 285.00, 320.00, 16, 4, "pack");
            seedProductIfMissing(shop4, "Probiotic Strawberry Greek Yogurt (400g)", "DAIRY-YOG-01", "Dairy & Fresh Beverages", 85.00, 99.00, 0, 5, "cup"); // Out-of-stock
        }

        // =========================================================================
        // 5. Smart General Store (Suggested Shop 5 - Location B: East Pune)
        // =========================================================================
        User retailerB2 = seedUserIfMissing("Sneha Joshi", "smartstore@smartlocal.com", "Retailer@123", "+91 98765 43214", Role.ROLE_RETAILER);
        if (retailerB2 != null) {
            Shop shop5 = shopRepository.findByOwner(retailerB2).orElseGet(() -> {
                Shop s = new Shop(
                        "Smart General Store",
                        retailerB2.getFullName(),
                        "+91 98765 43214",
                        "Shop #14, Viman Nagar Boulevard, East Pune",
                        "Consumer Electronics & Household Essentials",
                        "LOC-PUNE-B",
                        "East Pune",
                        "Comprehensive neighborhood retailer offering electronics accessories, healthcare products, and packaged groceries.",
                        "08:30 AM - 10:00 PM",
                        ApprovalStatus.APPROVED,
                        retailerB2
                );
                return shopRepository.save(s);
            });
            shop5.setShopName("Smart General Store");
            shop5.setLocationId("LOC-PUNE-B");
            shop5.setLocationName("East Pune");
            shop5.setAddress("Shop #14, Viman Nagar Boulevard, East Pune");
            shop5.setCategory("Consumer Electronics & Household Essentials");
            shop5.setLatitude(18.5665);
            shop5.setLongitude(73.9122);
            shop5.setActive(true);
            shop5.setApprovalStatus(ApprovalStatus.APPROVED);
            shopRepository.save(shop5);

            // Cross-store price comparisons in Location B
            seedProductIfMissing(shop5, "Amul Taaza Homogenised Toned Milk (1L)", "DAIRY-MLK-02", "Dairy & Fresh Beverages", 75.00, 75.00, 18, 5, "carton");
            seedProductIfMissing(shop5, "Pure Buffalo Desi Ghee (1L)", "DAIRY-GHEE-02", "Dairy & Fresh Beverages", 640.00, 680.00, 3, 5, "jar"); // Low-stock
            seedProductIfMissing(shop5, "Fresh Malai Paneer (500g)", "DAIRY-PAN-02", "Dairy & Fresh Beverages", 180.00, 220.00, 0, 5, "pack"); // Out-of-stock
            seedProductIfMissing(shop5, "Fast-Charging USB-C Braided Cable (2m)", "TECH-CAB-03", "Consumer Electronics & Household Essentials", 249.00, 499.00, 30, 5, "pcs"); // Lowest price
            seedProductIfMissing(shop5, "Paracetamol 650mg & Vitamin C Tablets", "PHARM-PARA-01", "Pharmacy & Healthcare", 65.00, 80.00, 50, 10, "strip");
            seedProductIfMissing(shop5, "Digital Infrared Thermometer", "TECH-THERM-01", "Consumer Electronics & Household Essentials", 1199.00, 1599.00, 2, 5, "pcs"); // Low-stock
        }

        // =========================================================================
        // 6. Heritage Book & Stationery Depot (Location A - Missing Phone Fallback)
        // =========================================================================
        User retailerA4 = seedUserIfMissing("Prakash Kulkarni", "stationery@smartlocal.com", "Retailer@123", "", Role.ROLE_RETAILER);
        if (retailerA4 != null) {
            Shop shop6 = shopRepository.findByOwner(retailerA4).orElseGet(() -> {
                Shop s = new Shop(
                        "Heritage Book & Stationery Depot",
                        retailerA4.getFullName(),
                        "", // Missing contact number for graceful handling verification
                        "Shop #22, Appa Balwant Chowk, Central Pune",
                        "Stationery & Books",
                        "LOC-PUNE-A",
                        "Central Pune",
                        "Traditional academic bookstore and stationery shop without registered telephone number.",
                        "10:00 AM - 08:30 PM",
                        ApprovalStatus.APPROVED,
                        retailerA4
                );
                return shopRepository.save(s);
            });
            shop6.setLocationId("LOC-PUNE-A");
            shop6.setLocationName("Central Pune");
            shop6.setAddress("Shop #22, Appa Balwant Chowk, Central Pune");
            shop6.setLatitude(18.5162);
            shop6.setLongitude(73.8558);
            shopRepository.save(shop6);

            seedProductIfMissing(shop6, "College Technical Notebook (300 pgs)", "STAT-NOTE-01", "Stationery & Books", 85.00, 100.00, 45, 10, "pcs");
        }

        // =========================================================================
        // 7. Anand Handloom & Organic Cottons (Location A - Pending Verification)
        // =========================================================================
        User retailerPending = seedUserIfMissing("Anand Verma", "anand.textiles@smartlocal.com", "Retailer@123", "+91 98765 43215", Role.ROLE_RETAILER);
        if (retailerPending != null) {
            Shop shop7 = shopRepository.findByOwner(retailerPending).orElseGet(() -> {
                Shop s = new Shop(
                        "Anand Handloom & Organic Cottons",
                        retailerPending.getFullName(),
                        "+91 98765 43215",
                        "Shop #12, Kumthekar Road, Central Pune",
                        "Handlooms, Beddings & Organic Cottons",
                        "LOC-PUNE-A",
                        "Central Pune",
                        "Handcrafted artisanal fabrics, organic cotton bedsheets, and local handlooms.",
                        "10:00 AM - 09:00 PM",
                        ApprovalStatus.PENDING,
                        retailerPending
                );
                return shopRepository.save(s);
            });
            shop7.setLocationId("LOC-PUNE-A");
            shop7.setLocationName("Central Pune");
            shop7.setAddress("Shop #12, Kumthekar Road, Central Pune");
            shop7.setLatitude(18.5190);
            shop7.setLongitude(73.8530);
            shopRepository.save(shop7);

            seedProductIfMissing(shop7, "Handcrafted Khadi Cotton Kurta", "TEXT-KUR-01", "Clothing & Organic Apparel", 899.00, 1299.00, 20, 5, "pcs");
        }
    }

    private boolean matchesLocation(String shopLoc, String targetLoc) {
        if ("ALL".equalsIgnoreCase(targetLoc) || targetLoc == null || targetLoc.trim().isEmpty()) return true;
        if (shopLoc == null) return false;
        if (shopLoc.equalsIgnoreCase(targetLoc)) return true;
        // Backwards compatibility for early demo keys
        if ("LOC-PUNE-A".equalsIgnoreCase(targetLoc) && ("LOC-DEMO-1".equalsIgnoreCase(shopLoc) || shopLoc.contains("Central"))) return true;
        if ("LOC-PUNE-B".equalsIgnoreCase(targetLoc) && ("LOC-DEMO-2".equalsIgnoreCase(shopLoc) || shopLoc.contains("East") || shopLoc.contains("Green"))) return true;
        return false;
    }

    private User seedUserIfMissing(String fullName, String email, String password, String phone, Role role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User user = new User(fullName, email, passwordEncoder.encode(password), phone, role);
            return userRepository.save(user);
        });
    }

    private void seedProductIfMissing(Shop shop, String name, String sku, String category, Double price, Double originalPrice, int qty, int threshold, String unit) {
        boolean exists = productRepository.findByShopOrderByCreatedAtDesc(shop).stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name));
        if (!exists) {
            Product p = new Product(name, sku, category, price, originalPrice, qty, threshold, unit, "Demonstration inventory item for " + shop.getShopName(), shop);
            productRepository.save(p);
        }
    }
}
