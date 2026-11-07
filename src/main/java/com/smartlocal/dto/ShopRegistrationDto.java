package com.smartlocal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for retailer shop registration and profile updates.
 */
public class ShopRegistrationDto {

    private Long id;

    @NotBlank(message = "Shop name is required")
    @Size(min = 2, max = 120, message = "Shop name must be between 2 and 120 characters")
    private String shopName;

    @NotBlank(message = "Owner information is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9+ -]{8,20}$", message = "Please enter a valid store contact number")
    private String contactNumber;

    @NotBlank(message = "Shop physical address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @NotBlank(message = "Store category is required")
    private String category;

    @NotBlank(message = "Please select a demonstration area")
    private String locationId;

    @Size(max = 60, message = "Opening hours must not exceed 60 characters")
    private String openingHours = "09:00 AM - 09:00 PM";

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    public ShopRegistrationDto() {
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

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
