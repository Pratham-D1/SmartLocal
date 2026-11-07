package com.smartlocal.service.impl;

import com.smartlocal.dto.PriceComparisonDto;
import com.smartlocal.dto.ProductCardDto;
import com.smartlocal.dto.ProductCategoryDto;
import com.smartlocal.dto.ShopPriceDto;
import com.smartlocal.entity.Product;
import com.smartlocal.repository.ProductRepository;
import com.smartlocal.service.CustomerProductService;
import com.smartlocal.service.SearchSynonymService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of CustomerProductService connecting live database inventory
 * with customer product search, category filtering, and price comparisons.
 */
@Service
public class CustomerProductServiceImpl implements CustomerProductService {

    private final ProductRepository productRepository;
    private final SearchSynonymService searchSynonymService;
    private final List<ProductCardDto> fallbackDemoProducts = new ArrayList<>();
    private final List<ProductCategoryDto> demoCategories = new ArrayList<>();

    @Autowired
    public CustomerProductServiceImpl(ProductRepository productRepository,
                                     SearchSynonymService searchSynonymService) {
        this.productRepository = productRepository;
        this.searchSynonymService = searchSynonymService;
        initCategories();
        initFallbackProducts();
    }

    private void initCategories() {
        demoCategories.add(new ProductCategoryDto("all", "All Categories", "bi-grid", 20));
        demoCategories.add(new ProductCategoryDto("Groceries & Daily Essentials", "Groceries", "bi-basket", 8));
        demoCategories.add(new ProductCategoryDto("Dairy & Fresh Beverages", "Dairy", "bi-cup-straw", 5));
        demoCategories.add(new ProductCategoryDto("Organic Fruits, Vegetables & Staples", "Organic", "bi-flower1", 4));
        demoCategories.add(new ProductCategoryDto("Consumer Electronics & Household Essentials", "Electronics", "bi-laptop", 6));
        demoCategories.add(new ProductCategoryDto("Pharmacy & Healthcare", "Pharmacy", "bi-capsule", 2));
    }

    private void initFallbackProducts() {
        ProductCardDto p1 = new ProductCardDto(
                101L,
                "Basmati Premium Rice (5 kg)",
                "Groceries & Daily Essentials",
                2L,
                "FreshDaily Supermart",
                "+91 98765 43211",
                "Plot #8, J.M. Road Market, Central Pune",
                420.00,
                490.00,
                38,
                true,
                "LOC-PUNE-A",
                "Central Pune",
                "Location not set",
                "Staple"
        );
        p1.setShopLat(18.5204);
        p1.setShopLng(73.8567);
        fallbackDemoProducts.add(p1);

        ProductCardDto p2 = new ProductCardDto(
                102L,
                "Paracetamol 650mg & Vitamin C Tablets",
                "Pharmacy & Healthcare",
                3L,
                "LifeCare Community Pharmacy",
                "+91 98765 43212",
                "Shop #3, Nagar Road Plaza, East Pune",
                65.00,
                80.00,
                85,
                true,
                "LOC-PUNE-B",
                "East Pune",
                "Location not set",
                "Healthcare"
        );
        p2.setShopLat(18.5679);
        p2.setShopLng(73.9143);
        fallbackDemoProducts.add(p2);
    }

    @Override
    public List<ProductCategoryDto> getCategories() {
        return demoCategories;
    }

    @Override
    public List<ProductCardDto> searchProducts(String keyword, String category, String locationId) {
        return searchProducts(keyword, category, locationId, null, null, null, "nearest");
    }

    @Override
    public List<ProductCardDto> searchProducts(String keyword, String category, String locationId, String sortBy) {
        return searchProducts(keyword, category, locationId, null, null, null, sortBy);
    }

    @Override
    public List<ProductCardDto> searchProducts(String keyword, String category, String locationId, Double userLat, Double userLng, Double maxRadiusKm, String sortBy) {
        List<ProductCardDto> results = new ArrayList<>();
        boolean hasUserCoords = isValidCoordinate(userLat, userLng);

        // 1. Query live database products (only from approved and active shops)
        List<Product> dbProducts = productRepository.findAll();
        for (Product p : dbProducts) {
            if (p.getShop() == null || (p.getShop().getApprovalStatus() == com.smartlocal.entity.ApprovalStatus.APPROVED && p.getShop().isActive())) {
                results.add(mapEntityToCardDto(p));
            }
        }

        // Add fallback items if database is empty
        if (dbProducts.isEmpty()) {
            results.addAll(fallbackDemoProducts);
        }

        // Calculate straight-line distances using Haversine formula
        for (ProductCardDto card : results) {
            boolean hasShopCoords = isValidCoordinate(card.getShopLat(), card.getShopLng());
            if (hasUserCoords && hasShopCoords) {
                double distanceKm = calculateDistanceKm(userLat, userLng, card.getShopLat(), card.getShopLng());
                card.setDistanceKm(distanceKm);
                if (distanceKm < 1.0) {
                    long meters = Math.round(distanceKm * 1000.0);
                    card.setDemoDistance(meters + " m away");
                    card.setFormattedStraightLineDistance("Approx. " + meters + " m straight-line distance");
                } else {
                    card.setDemoDistance(String.format(java.util.Locale.US, "%.1f km away", distanceKm));
                    card.setFormattedStraightLineDistance(String.format(java.util.Locale.US, "Approx. %.1f km straight-line distance", distanceKm));
                }
            } else if (!hasShopCoords) {
                card.setDistanceKm(null);
                card.setDemoDistance("Distance unavailable");
                card.setFormattedStraightLineDistance("Coordinates unavailable for this store");
            } else {
                card.setDistanceKm(null);
                card.setDemoDistance("Location not set");
                card.setFormattedStraightLineDistance("Select your location to calculate straight-line distance");
            }
        }

        // 2. Prepare synonym tokens for AI-assisted rule-based search
        List<String> searchTokens = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            searchTokens = searchSynonymService.getSearchTokens(keyword);
        }

        final List<String> finalSearchTokens = searchTokens;

        // 3. Filter results
        List<ProductCardDto> filtered = results.stream()
                .filter(p -> {
                    // Radius filter: when customer location coordinates and radius are set
                    if (maxRadiusKm != null && maxRadiusKm > 0) {
                        if (p.getDistanceKm() == null || p.getDistanceKm() > maxRadiusKm) {
                            return false;
                        }
                    }

                    // Location filter: strictly within the selected demo location when radius filter is not active
                    if ((maxRadiusKm == null || maxRadiusKm <= 0) && locationId != null && !locationId.trim().isEmpty() && !"all".equalsIgnoreCase(locationId)) {
                        if (!matchesLocation(p.getLocationId(), p.getLocationName(), locationId)) {
                            return false;
                        }
                    }

                    // Category filter
                    if (category != null && !category.trim().isEmpty() && !"all".equalsIgnoreCase(category)) {
                        boolean matchExact = p.getCategory().equalsIgnoreCase(category.trim());
                        boolean matchContains = p.getCategory().toLowerCase().contains(category.trim().toLowerCase());
                        if (!matchExact && !matchContains) {
                            return false;
                        }
                    }

                    // Keyword query filter with case-insensitive, partial-name matching and synonym expansion
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        String lowerKw = keyword.trim().toLowerCase();
                        String prodName = p.getName().toLowerCase();
                        String prodCat = p.getCategory().toLowerCase();
                        String shopName = p.getShopName().toLowerCase();

                        // Direct partial match on product name, category, or shop name
                        boolean matchesDirect = prodName.contains(lowerKw) || prodCat.contains(lowerKw) || shopName.contains(lowerKw);

                        // AI-assisted rule-based synonym token match
                        boolean matchesSynonym = false;
                        for (String token : finalSearchTokens) {
                            if (prodName.contains(token) || prodCat.contains(token) || shopName.contains(token)
                                    || token.contains(prodName)) {
                                matchesSynonym = true;
                                break;
                            }
                        }

                        return matchesDirect || matchesSynonym;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        // 4. Sort results
        String sortOption = (sortBy != null && !sortBy.trim().isEmpty()) ? sortBy.trim().toLowerCase() : (hasUserCoords ? "nearest" : "price_asc");
        if ("nearest".equalsIgnoreCase(sortOption) || "distance_asc".equalsIgnoreCase(sortOption)) {
            // Sort by straight-line distance ascending, then by price ascending
            filtered.sort(java.util.Comparator.comparing((ProductCardDto p) -> p.getDistanceKm() != null ? p.getDistanceKm() : 999999.0)
                    .thenComparingDouble(ProductCardDto::getPrice));
        } else if ("price_asc".equalsIgnoreCase(sortOption) || "lowest_price".equalsIgnoreCase(sortOption)) {
            // Sort by price ascending, then by distance
            filtered.sort(java.util.Comparator.comparingDouble(ProductCardDto::getPrice)
                    .thenComparing(p -> p.getDistanceKm() != null ? p.getDistanceKm() : 999999.0));
        } else if ("price_desc".equalsIgnoreCase(sortOption)) {
            filtered.sort(java.util.Comparator.comparingDouble(ProductCardDto::getPrice).reversed());
        } else if ("name_asc".equalsIgnoreCase(sortOption) || "name".equalsIgnoreCase(sortOption)) {
            filtered.sort(java.util.Comparator.comparing(p -> p.getName().toLowerCase()));
        } else {
            // Default: if coordinates are available, nearest; otherwise lowest price
            if (hasUserCoords) {
                filtered.sort(java.util.Comparator.comparing((ProductCardDto p) -> p.getDistanceKm() != null ? p.getDistanceKm() : 999999.0)
                        .thenComparingDouble(ProductCardDto::getPrice));
            } else {
                filtered.sort(java.util.Comparator.comparingDouble(ProductCardDto::getPrice));
            }
        }

        // 5. Highlight the cheapest available option
        // CRITICAL: Do not recommend an out-of-stock item as the cheapest available option
        double minAvailablePrice = filtered.stream()
                .filter(p -> p.isInStock() && p.getStockQuantity() > 0)
                .mapToDouble(ProductCardDto::getPrice)
                .min()
                .orElse(-1.0);

        for (ProductCardDto card : filtered) {
            // Strictly exclude out-of-stock or zero-quantity products from cheapest highlight
            if (minAvailablePrice > 0
                    && card.isInStock()
                    && card.getStockQuantity() > 0
                    && Math.abs(card.getPrice() - minAvailablePrice) < 0.001) {
                card.setCheapestOption(true);
            } else {
                card.setCheapestOption(false);
            }
        }

        return filtered;
    }

    private double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth's mean radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return Math.round((R * c) * 100.0) / 100.0;
    }

    private boolean isValidCoordinate(Double lat, Double lng) {
        return lat != null && lng != null
                && !lat.isNaN() && !lng.isNaN()
                && lat >= -90.0 && lat <= 90.0
                && lng >= -180.0 && lng <= 180.0;
    }

    private boolean matchesLocation(String itemLocId, String itemLocName, String targetLoc) {
        if (targetLoc == null || targetLoc.trim().isEmpty() || "all".equalsIgnoreCase(targetLoc)) {
            return true;
        }
        String target = targetLoc.trim();
        if (itemLocId != null && itemLocId.equalsIgnoreCase(target)) {
            return true;
        }
        // Normalize LOC-DEMO-1 / Location A
        if ("LOC-PUNE-A".equalsIgnoreCase(target) || "LOC-DEMO-1".equalsIgnoreCase(target)) {
            return ("LOC-PUNE-A".equalsIgnoreCase(itemLocId) || "LOC-DEMO-1".equalsIgnoreCase(itemLocId)
                    || (itemLocName != null && (itemLocName.contains("Central") || itemLocName.contains("Metro"))));
        }
        // Normalize LOC-PUNE-B / Location B
        if ("LOC-PUNE-B".equalsIgnoreCase(target) || "LOC-DEMO-2".equalsIgnoreCase(target)) {
            return ("LOC-PUNE-B".equalsIgnoreCase(itemLocId) || "LOC-DEMO-2".equalsIgnoreCase(itemLocId)
                    || (itemLocName != null && (itemLocName.contains("East") || itemLocName.contains("Green Valley"))));
        }
        return false;
    }

    @Override
    public Optional<PriceComparisonDto> getPriceComparisonForProduct(Long productId) {
        ProductCardDto product = getProductById(productId).orElse(null);
        if (product == null) {
            return Optional.empty();
        }

        List<ShopPriceDto> quotes = new ArrayList<>();
        quotes.add(new ShopPriceDto(
                product.getShopId(),
                product.getShopName(),
                product.getPrice(),
                product.getOriginalPrice(),
                product.isInStock() ? "In Stock (" + product.getStockQuantity() + " units)" : "Out of Stock",
                product.getStockQuantity(),
                product.getDemoDistance(),
                product.getLocationName(),
                product.getShopContact(),
                true
        ));

        // Generate comparative nearby store quote
        quotes.add(new ShopPriceDto(
                99L,
                "Metro Retail Superstore",
                product.getPrice() * 1.06,
                product.getOriginalPrice(),
                "In Stock (8 units)",
                8,
                "1.2 km away",
                product.getLocationName(),
                "+91 98765 43299",
                false
        ));

        double min = quotes.stream().mapToDouble(ShopPriceDto::getPrice).min().orElse(product.getPrice());
        double max = quotes.stream().mapToDouble(ShopPriceDto::getPrice).max().orElse(product.getPrice());

        PriceComparisonDto dto = new PriceComparisonDto(
                product.getId(),
                product.getName(),
                product.getCategory(),
                min,
                max,
                max - min,
                quotes
        );

        return Optional.of(dto);
    }

    @Override
    public Optional<ProductCardDto> getProductById(Long productId) {
        if (productId == null) return Optional.empty();

        // Check database first
        Optional<Product> dbProduct = productRepository.findById(productId);
        if (dbProduct.isPresent()) {
            return Optional.of(mapEntityToCardDto(dbProduct.get()));
        }

        return fallbackDemoProducts.stream()
                .filter(p -> p.getId().equals(productId))
                .findFirst();
    }

    private ProductCardDto mapEntityToCardDto(Product p) {
        String shopLocId = (p.getShop() != null && p.getShop().getLocationId() != null) ? p.getShop().getLocationId() : "LOC-PUNE-A";
        String shopLocName = (p.getShop() != null && p.getShop().getLocationName() != null) ? p.getShop().getLocationName() : "Central Pune";
        String shopAddress = (p.getShop() != null && p.getShop().getAddress() != null) ? p.getShop().getAddress() : "Central Pune";
        String shopName = (p.getShop() != null) ? p.getShop().getShopName() : "Local Retail Store";
        String shopContact = (p.getShop() != null && p.getShop().getContactNumber() != null) ? p.getShop().getContactNumber() : "+91 98765 43210";
        Long shopId = (p.getShop() != null) ? p.getShop().getId() : 1L;

        int qty = (p.getQuantity() != null) ? p.getQuantity() : 0;
        ProductCardDto dto = new ProductCardDto(
                p.getId(),
                p.getName(),
                p.getCategory(),
                shopId,
                shopName,
                shopContact,
                shopAddress,
                p.getPrice(),
                p.getOriginalPrice() != null ? p.getOriginalPrice() : p.getPrice(),
                qty,
                qty > 0,
                shopLocId,
                shopLocName,
                "Location not set",
                p.getStockStatus().getDisplayName()
        );
        dto.setUnit((p.getUnit() != null && !p.getUnit().trim().isEmpty()) ? p.getUnit() : "pcs");
        if (p.getShop() != null) {
            dto.setShopLat(p.getShop().getLatitude());
            dto.setShopLng(p.getShop().getLongitude());
        }
        return dto;
    }
}
