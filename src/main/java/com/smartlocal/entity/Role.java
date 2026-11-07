package com.smartlocal.entity;

/**
 * User roles supported by SmartLocal.
 */
public enum Role {
    ROLE_CUSTOMER("Customer"),
    ROLE_RETAILER("Retailer"),
    ROLE_ADMIN("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
