package com.smartlocal.service.impl;

import com.smartlocal.dto.AdminProductDetailDto;
import com.smartlocal.dto.AdminShopDetailDto;
import com.smartlocal.dto.AdminStatsDto;
import com.smartlocal.dto.AdminUserDetailDto;
import com.smartlocal.entity.ApprovalStatus;
import com.smartlocal.entity.Product;
import com.smartlocal.entity.Role;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;
import com.smartlocal.repository.ProductRepository;
import com.smartlocal.repository.ShopRepository;
import com.smartlocal.repository.UserRepository;
import com.smartlocal.service.AdminService;
import com.smartlocal.service.ShopDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of AdminService for system administration,
 * store verification, active status toggling, and user auditing.
 */
@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final ShopDiscoveryService shopDiscoveryService;

    @Autowired
    public AdminServiceImpl(UserRepository userRepository,
                            ShopRepository shopRepository,
                            ProductRepository productRepository,
                            ShopDiscoveryService shopDiscoveryService) {
        this.userRepository = userRepository;
        this.shopRepository = shopRepository;
        this.productRepository = productRepository;
        this.shopDiscoveryService = shopDiscoveryService;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsDto getSystemStatistics() {
        AdminStatsDto stats = new AdminStatsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setTotalCustomers(userRepository.countByRole(Role.ROLE_CUSTOMER));
        stats.setTotalRetailers(userRepository.countByRole(Role.ROLE_RETAILER));
        stats.setTotalAdmins(userRepository.countByRole(Role.ROLE_ADMIN));

        stats.setTotalShops(shopRepository.count());
        stats.setApprovedShops(shopRepository.countByApprovalStatus(ApprovalStatus.APPROVED));
        stats.setPendingShops(shopRepository.countByApprovalStatus(ApprovalStatus.PENDING));
        stats.setRejectedShops(shopRepository.countByApprovalStatus(ApprovalStatus.REJECTED));
        stats.setActiveShops(shopRepository.countByActive(true));
        stats.setInactiveShops(shopRepository.countByActive(false));

        stats.setTotalProducts(productRepository.count());
        stats.setInStockProducts(productRepository.countByQuantityGreaterThan(0));
        stats.setOutOfStockProducts(productRepository.countByQuantity(0));
        stats.setTotalLocations(shopDiscoveryService.getDemoLocations().size());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDetailDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.sort(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        List<AdminUserDetailDto> dtoList = new ArrayList<>();
        for (User u : users) {
            String shopName = null;
            if (u.getRole() == Role.ROLE_RETAILER) {
                Optional<Shop> shopOpt = shopRepository.findByOwner(u);
                if (shopOpt.isPresent()) {
                    shopName = shopOpt.get().getShopName();
                }
            }
            dtoList.add(new AdminUserDetailDto(
                    u.getId(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getPhone(),
                    u.getRole(),
                    u.isEnabled(),
                    u.getCreatedAt(),
                    shopName
            ));
        }
        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDetailDto> getCustomers() {
        return getAllUsers().stream()
                .filter(AdminUserDetailDto::isCustomer)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminUserDetailDto> getRetailers() {
        return getAllUsers().stream()
                .filter(AdminUserDetailDto::isRetailer)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminShopDetailDto> getAllShops() {
        List<Shop> shops = shopRepository.findAll();
        shops.sort(Comparator.comparing(Shop::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        List<AdminShopDetailDto> dtoList = new ArrayList<>();
        for (Shop s : shops) {
            long pCount = productRepository.countByShop(s);
            String ownerEmail = s.getOwner() != null ? s.getOwner().getEmail() : "N/A";
            dtoList.add(new AdminShopDetailDto(
                    s.getId(),
                    s.getShopName(),
                    s.getOwnerName(),
                    ownerEmail,
                    s.getContactNumber(),
                    s.getAddress(),
                    s.getCategory(),
                    s.getLocationId(),
                    s.getLocationName(),
                    s.getApprovalStatus(),
                    s.isActive(),
                    (int) pCount,
                    s.getCreatedAt()
            ));
        }
        return dtoList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminShopDetailDto> getPendingShops() {
        return getAllShops().stream()
                .filter(AdminShopDetailDto::isPending)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProductDetailDto> getAllProducts() {
        List<Product> products = productRepository.findAll();
        products.sort(Comparator.comparing(Product::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())));

        List<AdminProductDetailDto> dtoList = new ArrayList<>();
        for (Product p : products) {
            String shopName = p.getShop() != null ? p.getShop().getShopName() : "Unassigned";
            String locName = p.getShop() != null ? p.getShop().getLocationName() : "N/A";
            String status = p.getStockStatus() != null ? p.getStockStatus().getDisplayName() : "Unknown";

            dtoList.add(new AdminProductDetailDto(
                    p.getId(),
                    p.getName(),
                    p.getSku(),
                    p.getCategory(),
                    p.getPrice(),
                    p.getOriginalPrice(),
                    p.getQuantity(),
                    p.getLowStockThreshold(),
                    p.getUnit(),
                    shopName,
                    locName,
                    status,
                    p.getCreatedAt()
            ));
        }
        return dtoList;
    }

    @Override
    @Transactional
    public Shop approveShop(Long shopId, String adminEmail) {
        verifyAdminAuthorization(adminEmail);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));
        shop.setApprovalStatus(ApprovalStatus.APPROVED);
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop rejectShop(Long shopId, String adminEmail) {
        verifyAdminAuthorization(adminEmail);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));
        shop.setApprovalStatus(ApprovalStatus.REJECTED);
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public Shop toggleShopActive(Long shopId, String adminEmail) {
        verifyAdminAuthorization(adminEmail);
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new IllegalArgumentException("Shop not found with ID: " + shopId));
        shop.setActive(!shop.isActive());
        return shopRepository.save(shop);
    }

    @Override
    @Transactional
    public User toggleUserStatus(Long userId, String adminEmail) {
        verifyAdminAuthorization(adminEmail);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        if (user.getEmail().equalsIgnoreCase(adminEmail)) {
            throw new IllegalArgumentException("Operation Blocked: You cannot deactivate your own active administrator account.");
        }

        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }

    private void verifyAdminAuthorization(String adminEmail) {
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            throw new AccessDeniedException("Unauthorized: Administrative principal required.");
        }
        User admin = userRepository.findByEmail(adminEmail.trim().toLowerCase())
                .orElseThrow(() -> new AccessDeniedException("Unauthorized: Account not recognized."));
        if (admin.getRole() != Role.ROLE_ADMIN) {
            throw new AccessDeniedException("Security Violation: Elevated ROLE_ADMIN privileges strictly required for this operation.");
        }
    }
}
