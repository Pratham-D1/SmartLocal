package com.smartlocal.service;

import com.smartlocal.dto.AdminProductDetailDto;
import com.smartlocal.dto.AdminShopDetailDto;
import com.smartlocal.dto.AdminStatsDto;
import com.smartlocal.dto.AdminUserDetailDto;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;

import java.util.List;

/**
 * Service managing administrative operations, system statistics,
 * shop approvals, active/inactive shop toggling, and user directory management.
 */
public interface AdminService {

    AdminStatsDto getSystemStatistics();

    List<AdminUserDetailDto> getAllUsers();

    List<AdminUserDetailDto> getCustomers();

    List<AdminUserDetailDto> getRetailers();

    List<AdminShopDetailDto> getAllShops();

    List<AdminShopDetailDto> getPendingShops();

    List<AdminProductDetailDto> getAllProducts();

    Shop approveShop(Long shopId, String adminEmail);

    Shop rejectShop(Long shopId, String adminEmail);

    Shop toggleShopActive(Long shopId, String adminEmail);

    User toggleUserStatus(Long userId, String adminEmail);
}
