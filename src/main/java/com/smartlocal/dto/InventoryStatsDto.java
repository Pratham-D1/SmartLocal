package com.smartlocal.dto;

/**
 * Data Transfer Object summarizing inventory health metrics for the retailer dashboard.
 */
public class InventoryStatsDto {
    private int totalProducts;
    private int inStockCount;
    private int lowStockCount;
    private int outOfStockCount;
    private int totalStockUnits;
    private double totalValuation;

    public InventoryStatsDto() {
    }

    public InventoryStatsDto(int totalProducts, int inStockCount, int lowStockCount,
                             int outOfStockCount, int totalStockUnits, double totalValuation) {
        this.totalProducts = totalProducts;
        this.inStockCount = inStockCount;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
        this.totalStockUnits = totalStockUnits;
        this.totalValuation = totalValuation;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getInStockCount() {
        return inStockCount;
    }

    public void setInStockCount(int inStockCount) {
        this.inStockCount = inStockCount;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public int getOutOfStockCount() {
        return outOfStockCount;
    }

    public void setOutOfStockCount(int outOfStockCount) {
        this.outOfStockCount = outOfStockCount;
    }

    public int getTotalStockUnits() {
        return totalStockUnits;
    }

    public void setTotalStockUnits(int totalStockUnits) {
        this.totalStockUnits = totalStockUnits;
    }

    public double getTotalValuation() {
        return totalValuation;
    }

    public void setTotalValuation(double totalValuation) {
        this.totalValuation = totalValuation;
    }
}
