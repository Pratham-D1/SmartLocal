package com.smartlocal.entity;

/**
 * Stock availability classification for inventory management and customer discovery.
 */
public enum StockStatus {
    IN_STOCK("In Stock", "bg-success-subtle text-success border-success-subtle"),
    LOW_STOCK("Low Stock", "bg-warning-subtle text-warning border-warning-subtle"),
    OUT_OF_STOCK("Out of Stock", "bg-danger-subtle text-danger border-danger-subtle");

    private final String displayName;
    private final String badgeCssClass;

    StockStatus(String displayName, String badgeCssClass) {
        this.displayName = displayName;
        this.badgeCssClass = badgeCssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBadgeCssClass() {
        return badgeCssClass;
    }
}
