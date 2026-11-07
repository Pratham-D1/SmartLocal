package com.smartlocal.service;

import com.smartlocal.dto.ShopRegistrationDto;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;

import java.util.Optional;

/**
 * Service managing retailer shops, registration workflow,
 * profile updates, and strict ownership authorization.
 */
public interface ShopService {

    /**
     * Registers a new shop or updates an existing shop for the authenticated retailer.
     * Enforces that a retailer can only register or edit their own shop.
     */
    Shop registerOrUpdateShop(User retailerUser, ShopRegistrationDto dto);

    /**
     * Retrieves the shop associated with the given retailer account.
     */
    Optional<Shop> getShopByOwner(User retailerUser);

    /**
     * Retrieves a shop by ID, enforcing that the requesting retailer is the owner.
     */
    Shop getShopByIdForOwner(Long shopId, User retailerUser);

    /**
     * Seeds the initial demonstration shop for the default retailer account.
     */
    void initializeDefaultRetailerShop(User defaultRetailer);
}
