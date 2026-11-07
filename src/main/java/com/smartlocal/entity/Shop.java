package com.smartlocal.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a local retail store associated with a Retailer account.
 * Enforces one-to-one relationship between a retailer user and their shop.
 */
@Entity
@Table(name = "shops", uniqueConstraints = {
        @UniqueConstraint(columnNames = "owner_id", name = "uk_shops_owner")
})
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String shopName;

    @Column(nullable = false, length = 100)
    private String ownerName;

    @Column(nullable = false, length = 20)
    private String contactNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 50)
    private String locationId;

    @Column(nullable = false, length = 120)
    private String locationName;

    @Column(length = 80)
    private String city = "Pune";

    @Column(length = 120)
    private String locality;

    @Column(length = 500)
    private String description;

    @Column(length = 60)
    private String openingHours = "09:00 AM - 09:00 PM";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApprovalStatus approvalStatus = ApprovalStatus.PENDING;

    @Column(nullable = false)
    private boolean active = true;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Shop() {
    }

    public Shop(String shopName, String ownerName, String contactNumber, String address,
                String category, String locationId, String locationName, String description,
                String openingHours, ApprovalStatus approvalStatus, User owner) {
        this.shopName = shopName;
        this.ownerName = ownerName;
        this.contactNumber = contactNumber;
        this.address = address;
        this.category = category;
        this.locationId = locationId;
        this.locationName = locationName;
        this.description = description;
        this.openingHours = openingHours;
        this.approvalStatus = approvalStatus;
        this.owner = owner;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
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
        if (this.locality == null) {
            this.locality = locationName;
        }
    }

    public String getCity() {
        return (city != null && !city.isBlank()) ? city : "Pune";
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocality() {
        return (locality != null && !locality.isBlank()) ? locality : locationName;
    }

    public void setLocality(String locality) {
        this.locality = locality;
        if (this.locationName == null) {
            this.locationName = locality;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
