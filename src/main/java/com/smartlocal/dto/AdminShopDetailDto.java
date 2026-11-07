package com.smartlocal.dto;

import com.smartlocal.entity.ApprovalStatus;
import java.time.LocalDateTime;

/**
 * Detailed shop representation for Administrator Dashboard management and approvals.
 */
public class AdminShopDetailDto {

    private Long id;
    private String shopName;
    private String ownerName;
    private String ownerEmail;
    private String contactNumber;
    private String address;
    private String category;
    private String locationId;
    private String locationName;
    private ApprovalStatus approvalStatus;
    private boolean active;
    private int productCount;
    private LocalDateTime createdAt;

    public AdminShopDetailDto() {
    }

    public AdminShopDetailDto(Long id, String shopName, String ownerName, String ownerEmail,
                              String contactNumber, String address, String category,
                              String locationId, String locationName, ApprovalStatus approvalStatus,
                              boolean active, int productCount, LocalDateTime createdAt) {
        this.id = id;
        this.shopName = shopName;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
        this.contactNumber = contactNumber;
        this.address = address;
        this.category = category;
        this.locationId = locationId;
        this.locationName = locationName;
        this.approvalStatus = approvalStatus;
        this.active = active;
        this.productCount = productCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isPending() {
        return ApprovalStatus.PENDING == approvalStatus;
    }

    public boolean isApproved() {
        return ApprovalStatus.APPROVED == approvalStatus;
    }

    public boolean isRejected() {
        return ApprovalStatus.REJECTED == approvalStatus;
    }
}
