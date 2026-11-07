package com.smartlocal.service.impl;

import com.smartlocal.dto.ShopRegistrationDto;
import com.smartlocal.entity.ApprovalStatus;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;
import com.smartlocal.repository.ShopRepository;
import com.smartlocal.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of ShopService enforcing owner-level data isolation,
 * shop registration workflow, and approval status tracking.
 */
@Service
public class ShopServiceImpl implements ShopService {

    private final ShopRepository shopRepository;
    private final com.smartlocal.repository.UserRepository userRepository;

    @Autowired
    public ShopServiceImpl(ShopRepository shopRepository, com.smartlocal.repository.UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
    }

    @jakarta.annotation.PostConstruct
    @Transactional
    public void setupDemoShopOnStartup() {
        userRepository.findByEmail("retailer@smartlocal.com").ifPresent(this::initializeDefaultRetailerShop);
    }

    @Override
    @Transactional
    public Shop registerOrUpdateShop(User retailerUser, ShopRegistrationDto dto) {
        if (retailerUser == null) {
            throw new IllegalArgumentException("Retailer user context is required");
        }

        Shop shop;
        Optional<Shop> existingOwnerShop = shopRepository.findByOwner(retailerUser);

        // Security check: if an ID is supplied or the retailer already has a shop
        if (dto.getId() != null) {
            shop = shopRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + dto.getId()));

            // Strict Backend Ownership Authorization: Retailer cannot modify another retailer's shop
            if (!shop.getOwner().getId().equals(retailerUser.getId())) {
                throw new AccessDeniedException("Security Violation: You do not possess ownership authorization for this shop.");
            }
        } else if (existingOwnerShop.isPresent()) {
            // Update existing shop if one is already associated with this retailer
            shop = existingOwnerShop.get();
        } else {
            // Create a brand new shop
            shop = new Shop();
            shop.setOwner(retailerUser);
            // Default approval status: APPROVED for verified platform onboarding
            shop.setApprovalStatus(ApprovalStatus.APPROVED);
        }

        // Apply updated attributes
        shop.setShopName(dto.getShopName().trim());
        shop.setOwnerName(dto.getOwnerName().trim());
        shop.setContactNumber(dto.getContactNumber().trim());
        shop.setAddress(dto.getAddress().trim());
        shop.setCategory(dto.getCategory().trim());
        shop.setLocationId(dto.getLocationId().trim());
        shop.setLocationName(resolveLocationName(dto.getLocationId().trim()));
        shop.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : "");
        shop.setOpeningHours(dto.getOpeningHours() != null && !dto.getOpeningHours().trim().isEmpty()
                ? dto.getOpeningHours().trim() : "09:00 AM - 09:00 PM");

        return shopRepository.save(shop);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shop> getShopByOwner(User retailerUser) {
        if (retailerUser == null) {
            return Optional.empty();
        }
        return shopRepository.findByOwner(retailerUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Shop getShopByIdForOwner(Long shopId, User retailerUser) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));

        // Enforce backend authorization so retailers cannot view/modify another retailer's private settings
        if (!shop.getOwner().getId().equals(retailerUser.getId())) {
            throw new AccessDeniedException("Access Denied: You are not authorized to access another retailer's store data.");
        }
        return shop;
    }

    @Override
    @Transactional
    public void initializeDefaultRetailerShop(User defaultRetailer) {
        if (defaultRetailer != null && !shopRepository.existsByOwner(defaultRetailer)) {
            Shop demoShop = new Shop(
                    "Fresh Mart",
                    defaultRetailer.getFullName(),
                    "+91 98765 43210",
                    "Shop #10, F.C. Road Arcade, Central Pune",
                    "Groceries & Daily Essentials",
                    "LOC-PUNE-A",
                    "Central Pune",
                    "Premier neighborhood store for fresh staples, flours, pulses, and kitchen daily essentials.",
                    "08:30 AM - 09:30 PM",
                    ApprovalStatus.APPROVED,
                    defaultRetailer
            );
            demoShop.setLatitude(18.5204);
            demoShop.setLongitude(73.8567);
            shopRepository.save(demoShop);
        }
    }

    private String resolveLocationName(String locationId) {
        if ("LOC-PUNE-B".equalsIgnoreCase(locationId) || "LOC-DEMO-2".equalsIgnoreCase(locationId)) {
            return "East Pune";
        }
        return "Central Pune";
    }
}
