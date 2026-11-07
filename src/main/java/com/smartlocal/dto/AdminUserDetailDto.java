package com.smartlocal.dto;

import com.smartlocal.entity.Role;
import java.time.LocalDateTime;

/**
 * Detailed user representation for Administrator Dashboard user management.
 */
public class AdminUserDetailDto {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
    private String associatedShopName;

    public AdminUserDetailDto() {
    }

    public AdminUserDetailDto(Long id, String fullName, String email, String phone,
                              Role role, boolean enabled, LocalDateTime createdAt,
                              String associatedShopName) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.associatedShopName = associatedShopName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAssociatedShopName() {
        return associatedShopName;
    }

    public void setAssociatedShopName(String associatedShopName) {
        this.associatedShopName = associatedShopName;
    }

    public boolean isCustomer() {
        return Role.ROLE_CUSTOMER == role;
    }

    public boolean isRetailer() {
        return Role.ROLE_RETAILER == role;
    }

    public boolean isAdmin() {
        return Role.ROLE_ADMIN == role;
    }
}
