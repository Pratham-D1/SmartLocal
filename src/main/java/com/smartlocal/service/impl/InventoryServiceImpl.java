package com.smartlocal.service.impl;

import com.smartlocal.dto.InventoryStatsDto;
import com.smartlocal.dto.ProductDto;
import com.smartlocal.entity.Product;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.StockStatus;
import com.smartlocal.entity.User;
import com.smartlocal.repository.ProductRepository;
import com.smartlocal.repository.ShopRepository;
import com.smartlocal.service.InventoryService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of InventoryService enforcing shop-level ownership,
 * non-negative price/stock constraints, and dynamic stock status calculation.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;

    @Autowired
    public InventoryServiceImpl(ProductRepository productRepository, ShopRepository shopRepository) {
        this.productRepository = productRepository;
        this.shopRepository = shopRepository;
    }

    @Override
    @Transactional
    public Product addProduct(User retailerUser, ProductDto dto) {
        Shop shop = getRetailerShopOrThrow(retailerUser);
        validateProductDto(dto);

        Product product = new Product(
                dto.getName().trim(),
                dto.getSku() != null ? dto.getSku().trim() : "",
                dto.getCategory().trim(),
                dto.getPrice(),
                dto.getOriginalPrice() != null ? dto.getOriginalPrice() : dto.getPrice(),
                dto.getQuantity(),
                dto.getLowStockThreshold(),
                dto.getUnit() != null ? dto.getUnit().trim() : "pcs",
                dto.getDescription() != null ? dto.getDescription().trim() : "",
                shop
        );

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product updateProduct(User retailerUser, Long productId, ProductDto dto) {
        Shop shop = getRetailerShopOrThrow(retailerUser);
        Product product = getProductAndEnforceOwnership(productId, shop);
        validateProductDto(dto);

        product.setName(dto.getName().trim());
        product.setSku(dto.getSku() != null ? dto.getSku().trim() : "");
        product.setCategory(dto.getCategory().trim());
        product.setPrice(dto.getPrice());
        product.setOriginalPrice(dto.getOriginalPrice() != null ? dto.getOriginalPrice() : dto.getPrice());
        product.setQuantity(dto.getQuantity());
        product.setLowStockThreshold(dto.getLowStockThreshold());
        product.setUnit(dto.getUnit() != null ? dto.getUnit().trim() : "pcs");
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(User retailerUser, Long productId) {
        Shop shop = getRetailerShopOrThrow(retailerUser);
        Product product = getProductAndEnforceOwnership(productId, shop);
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductForRetailer(User retailerUser, Long productId) {
        Shop shop = getRetailerShopOrThrow(retailerUser);
        return getProductAndEnforceOwnership(productId, shop);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> getRetailerInventory(User retailerUser, String keyword, String category, String stockFilter) {
        Shop shop = getRetailerShopOrThrow(retailerUser);

        String cleanKw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCat = (category != null && !category.trim().isEmpty()) ? category.trim() : null;

        List<Product> products = productRepository.searchInventory(shop, cleanKw, cleanCat);

        // Apply stock status filter if requested
        if (stockFilter != null && !stockFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(stockFilter)) {
            products = products.stream()
                    .filter(p -> p.getStockStatus().name().equalsIgnoreCase(stockFilter.trim()))
                    .collect(Collectors.toList());
        }

        return products;
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryStatsDto getInventoryStats(User retailerUser) {
        Shop shop = getRetailerShopOrThrow(retailerUser);
        List<Product> allProducts = productRepository.findByShopOrderByCreatedAtDesc(shop);

        int totalProducts = allProducts.size();
        int inStockCount = 0;
        int lowStockCount = 0;
        int outOfStockCount = 0;
        int totalUnits = 0;
        double totalValuation = 0.0;

        for (Product p : allProducts) {
            StockStatus status = p.getStockStatus();
            if (status == StockStatus.IN_STOCK) {
                inStockCount++;
            } else if (status == StockStatus.LOW_STOCK) {
                lowStockCount++;
            } else if (status == StockStatus.OUT_OF_STOCK) {
                outOfStockCount++;
            }

            int qty = p.getQuantity() != null ? p.getQuantity() : 0;
            totalUnits += qty;
            totalValuation += (qty * (p.getPrice() != null ? p.getPrice() : 0.0));
        }

        return new InventoryStatsDto(totalProducts, inStockCount, lowStockCount, outOfStockCount, totalUnits, totalValuation);
    }

    @Override
    @Transactional
    public Product quickUpdate(User retailerUser, Long productId, Integer quantity, Double price) {
        Shop shop = getRetailerShopOrThrow(retailerUser);
        Product product = getProductAndEnforceOwnership(productId, shop);

        if (quantity != null) {
            if (quantity < 0) throw new IllegalArgumentException("Stock quantity cannot be negative.");
            product.setQuantity(quantity);
        }
        if (price != null) {
            if (price < 0.0) throw new IllegalArgumentException("Price cannot be negative.");
            product.setPrice(price);
        }

        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void initializeDefaultInventoryIfEmpty(Shop defaultShop) {
        if (defaultShop != null && productRepository.countByShop(defaultShop) == 0) {
            productRepository.save(new Product(
                    "Wireless Ergonomic Mouse",
                    "TECH-MOU-01",
                    "Consumer Electronics & Accessories",
                    849.00,
                    1199.00,
                    14,
                    5,
                    "pcs",
                    "2.4GHz wireless connectivity, 1600 DPI, silent click buttons, ergonomic thumb rest.",
                    defaultShop
            ));

            productRepository.save(new Product(
                    "Fast-Charging USB-C Braided Cable (2m)",
                    "TECH-CAB-02",
                    "Consumer Electronics & Accessories",
                    299.00,
                    499.00,
                    26,
                    8,
                    "pcs",
                    "60W PD fast charge supported, reinforced nylon braided jacket, 480 Mbps data transfer.",
                    defaultShop
            ));

            productRepository.save(new Product(
                    "Mechanical RGB Gaming Keyboard",
                    "TECH-KBD-03",
                    "Consumer Electronics & Accessories",
                    2499.00,
                    3499.00,
                    3,
                    5,
                    "pcs",
                    "Blue clicky switches, customizable per-key RGB backlighting, anti-ghosting keys.",
                    defaultShop
            ));

            productRepository.save(new Product(
                    "Bluetooth Noise-Cancelling Earbuds",
                    "TECH-EAR-04",
                    "Consumer Electronics & Accessories",
                    1899.00,
                    2999.00,
                    0,
                    5,
                    "pcs",
                    "Active noise cancellation, 30-hour total playback with charging case, IPX5 water resistant.",
                    defaultShop
            ));
        }
    }

    @PostConstruct
    @Transactional
    public void setupInitialStoreInventory() {
        shopRepository.findAll().stream().findFirst().ifPresent(this::initializeDefaultInventoryIfEmpty);
    }

    private Shop getRetailerShopOrThrow(User retailerUser) {
        return shopRepository.findByOwner(retailerUser)
                .orElseThrow(() -> new IllegalArgumentException("You must register your store profile before managing inventory."));
    }

    private Product getProductAndEnforceOwnership(Long productId, Shop shop) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // Strict Backend Ownership Authorization: Retailers cannot modify another shop's product
        if (!product.getShop().getId().equals(shop.getId())) {
            throw new AccessDeniedException("Security Violation: You do not possess ownership authorization for this product.");
        }
        return product;
    }

    private void validateProductDto(ProductDto dto) {
        if (dto.getPrice() == null || dto.getPrice() < 0) {
            throw new IllegalArgumentException("Product price must be a non-negative number.");
        }
        if (dto.getOriginalPrice() != null && dto.getOriginalPrice() < 0) {
            throw new IllegalArgumentException("Original price / MRP must be a non-negative number.");
        }
        if (dto.getQuantity() == null || dto.getQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity must be a non-negative integer.");
        }
        if (dto.getLowStockThreshold() == null || dto.getLowStockThreshold() < 0) {
            throw new IllegalArgumentException("Low-stock threshold must be a non-negative integer.");
        }
    }
}
