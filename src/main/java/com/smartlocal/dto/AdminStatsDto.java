package com.smartlocal.dto;

/**
 * Data Transfer Object for aggregated system metrics in the Administrator Dashboard.
 */
public class AdminStatsDto {

    private long totalUsers;
    private long totalCustomers;
    private long totalRetailers;
    private long totalAdmins;
    private long totalShops;
    private long approvedShops;
    private long pendingShops;
    private long rejectedShops;
    private long activeShops;
    private long inactiveShops;
    private long totalProducts;
    private long inStockProducts;
    private long outOfStockProducts;
    private int totalLocations;

    public AdminStatsDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(long totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public long getTotalRetailers() {
        return totalRetailers;
    }

    public void setTotalRetailers(long totalRetailers) {
        this.totalRetailers = totalRetailers;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getTotalShops() {
        return totalShops;
    }

    public void setTotalShops(long totalShops) {
        this.totalShops = totalShops;
    }

    public long getApprovedShops() {
        return approvedShops;
    }

    public void setApprovedShops(long approvedShops) {
        this.approvedShops = approvedShops;
    }

    public long getPendingShops() {
        return pendingShops;
    }

    public void setPendingShops(long pendingShops) {
        this.pendingShops = pendingShops;
    }

    public long getRejectedShops() {
        return rejectedShops;
    }

    public void setRejectedShops(long rejectedShops) {
        this.rejectedShops = rejectedShops;
    }

    public long getActiveShops() {
        return activeShops;
    }

    public void setActiveShops(long activeShops) {
        this.activeShops = activeShops;
    }

    public long getInactiveShops() {
        return inactiveShops;
    }

    public void setInactiveShops(long inactiveShops) {
        this.inactiveShops = inactiveShops;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getInStockProducts() {
        return inStockProducts;
    }

    public void setInStockProducts(long inStockProducts) {
        this.inStockProducts = inStockProducts;
    }

    public long getOutOfStockProducts() {
        return outOfStockProducts;
    }

    public void setOutOfStockProducts(long outOfStockProducts) {
        this.outOfStockProducts = outOfStockProducts;
    }

    public int getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(int totalLocations) {
        this.totalLocations = totalLocations;
    }
}
