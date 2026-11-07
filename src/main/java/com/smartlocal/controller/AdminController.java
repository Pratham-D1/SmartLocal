package com.smartlocal.controller;

import com.smartlocal.dto.AdminProductDetailDto;
import com.smartlocal.dto.AdminShopDetailDto;
import com.smartlocal.dto.AdminStatsDto;
import com.smartlocal.dto.AdminUserDetailDto;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;
import com.smartlocal.service.AdminService;
import com.smartlocal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller serving the Protected Administrator Dashboard (Module 9).
 * Features:
 * - View registered users, customers, and retailers.
 * - View shops with approval status and operational active status.
 * - View product catalog across all stores.
 * - View basic application statistics (KPI counter cards).
 * - Approve or reject retailer shop registrations.
 * - Activate or deactivate shops.
 * - Toggle user account status.
 * - Backend authorization strictly enforced.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;

    @Autowired
    public AdminController(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
    }

    /**
     * Administrator Dashboard Landing and Tab Controller.
     */
    @GetMapping("/dashboard")
    public String adminDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "tab", defaultValue = "overview") String tab,
            @RequestParam(value = "userFilter", defaultValue = "all") String userFilter,
            @RequestParam(value = "shopFilter", defaultValue = "all") String shopFilter,
            Model model) {

        populateAdminUser(userDetails, model);

        // Basic application statistics
        AdminStatsDto stats = adminService.getSystemStatistics();
        model.addAttribute("stats", stats);

        // Active tab & filter state
        model.addAttribute("activeTab", tab);
        model.addAttribute("userFilter", userFilter);
        model.addAttribute("shopFilter", shopFilter);

        // User Directory Data
        List<AdminUserDetailDto> allUsers = adminService.getAllUsers();
        model.addAttribute("allUsersCount", allUsers.size());
        model.addAttribute("customersList", adminService.getCustomers());
        model.addAttribute("retailersList", adminService.getRetailers());

        List<AdminUserDetailDto> displayedUsers = allUsers;
        if ("customers".equalsIgnoreCase(userFilter)) {
            displayedUsers = allUsers.stream().filter(AdminUserDetailDto::isCustomer).collect(Collectors.toList());
        } else if ("retailers".equalsIgnoreCase(userFilter)) {
            displayedUsers = allUsers.stream().filter(AdminUserDetailDto::isRetailer).collect(Collectors.toList());
        }
        model.addAttribute("displayedUsers", displayedUsers);

        // Shops Directory & Approvals Data
        List<AdminShopDetailDto> allShops = adminService.getAllShops();
        model.addAttribute("pendingShopsList", adminService.getPendingShops());

        List<AdminShopDetailDto> displayedShops = allShops;
        if ("pending".equalsIgnoreCase(shopFilter)) {
            displayedShops = allShops.stream().filter(AdminShopDetailDto::isPending).collect(Collectors.toList());
        } else if ("approved".equalsIgnoreCase(shopFilter)) {
            displayedShops = allShops.stream().filter(AdminShopDetailDto::isApproved).collect(Collectors.toList());
        } else if ("rejected".equalsIgnoreCase(shopFilter)) {
            displayedShops = allShops.stream().filter(AdminShopDetailDto::isRejected).collect(Collectors.toList());
        } else if ("active".equalsIgnoreCase(shopFilter)) {
            displayedShops = allShops.stream().filter(AdminShopDetailDto::isActive).collect(Collectors.toList());
        } else if ("inactive".equalsIgnoreCase(shopFilter)) {
            displayedShops = allShops.stream().filter(s -> !s.isActive()).collect(Collectors.toList());
        }
        model.addAttribute("displayedShops", displayedShops);

        // Product Catalog Data
        List<AdminProductDetailDto> allProducts = adminService.getAllProducts();
        model.addAttribute("allProducts", allProducts);

        return "dashboard/admin-dashboard";
    }

    /**
     * Approve Retailer Shop Registration.
     */
    @PostMapping("/shops/{id}/approve")
    public String approveShop(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Shop shop = adminService.approveShop(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Shop '" + shop.getShopName() + "' was successfully APPROVED and verified for public customer discovery!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=shops";
    }

    /**
     * Reject Retailer Shop Registration.
     */
    @PostMapping("/shops/{id}/reject")
    public String rejectShop(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Shop shop = adminService.rejectShop(id, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("warningMessage",
                    "Shop '" + shop.getShopName() + "' was REJECTED. It will be hidden from customer search results.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=shops";
    }

    /**
     * Toggle Shop Active/Inactive Operational Status.
     */
    @PostMapping("/shops/{id}/toggle-active")
    public String toggleShopActive(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Shop shop = adminService.toggleShopActive(id, userDetails.getUsername());
            String status = shop.isActive() ? "ACTIVATED" : "DEACTIVATED";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Shop '" + shop.getShopName() + "' has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=shops";
    }

    /**
     * Toggle User Account Enabled/Disabled Status.
     */
    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            User user = adminService.toggleUserStatus(id, userDetails.getUsername());
            String status = user.isEnabled() ? "ENABLED" : "DISABLED";
            redirectAttributes.addFlashAttribute("successMessage",
                    "User account '" + user.getEmail() + "' has been " + status + ".");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/dashboard?tab=users";
    }

    private void populateAdminUser(UserDetails userDetails, Model model) {
        if (userDetails != null) {
            Optional<User> userOpt = userService.findByEmail(userDetails.getUsername());
            userOpt.ifPresent(user -> model.addAttribute("currentUser", user));
        }
    }
}
