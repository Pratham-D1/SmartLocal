package com.smartlocal.controller;

import com.smartlocal.dto.InventoryStatsDto;
import com.smartlocal.dto.ProductDto;
import com.smartlocal.dto.ShopRegistrationDto;
import com.smartlocal.entity.Product;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;
import com.smartlocal.service.InventoryService;
import com.smartlocal.service.ShopDiscoveryService;
import com.smartlocal.service.ShopService;
import com.smartlocal.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller handling retailer dashboard, shop registration workflow,
 * and comprehensive inventory management operations.
 */
@Controller
public class RetailerController {

    private final UserService userService;
    private final ShopService shopService;
    private final ShopDiscoveryService shopDiscoveryService;
    private final InventoryService inventoryService;

    @Autowired
    public RetailerController(UserService userService,
                              ShopService shopService,
                              ShopDiscoveryService shopDiscoveryService,
                              InventoryService inventoryService) {
        this.userService = userService;
        this.shopService = shopService;
        this.shopDiscoveryService = shopDiscoveryService;
        this.inventoryService = inventoryService;
    }

    /**
     * Retailer Dashboard displaying shop profile, approval status,
     * and registration workflow if no shop is registered yet.
     */
    @GetMapping("/retailer/dashboard")
    public String retailerDashboard(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "saved", required = false) String saved,
            Model model) {

        User retailer = getAuthenticatedRetailer(userDetails);
        model.addAttribute("currentUser", retailer);

        Optional<Shop> shopOpt = shopService.getShopByOwner(retailer);
        boolean hasShop = shopOpt.isPresent();
        model.addAttribute("hasShop", hasShop);

        if (hasShop) {
            Shop shop = shopOpt.get();
            model.addAttribute("shop", shop);
            // Inventory summary stats for dashboard glance
            model.addAttribute("inventoryStats", inventoryService.getInventoryStats(retailer));
        } else {
            ShopRegistrationDto newShopDto = new ShopRegistrationDto();
            newShopDto.setOwnerName(retailer.getFullName());
            newShopDto.setContactNumber(retailer.getPhone());
            newShopDto.setLocationId("LOC-PUNE-A");
            newShopDto.setCategory("Consumer Electronics & Accessories");
            model.addAttribute("shopDto", newShopDto);
        }

        model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
        model.addAttribute("categories", getAvailableCategories());

        if (saved != null) {
            model.addAttribute("successMessage", "Shop profile saved and updated successfully!");
        }

        return "dashboard/retailer-dashboard";
    }

    /**
     * Dedicated Shop Profile Edit View.
     */
    @GetMapping("/retailer/shop/edit")
    public String editShopForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User retailer = getAuthenticatedRetailer(userDetails);
        model.addAttribute("currentUser", retailer);

        Optional<Shop> shopOpt = shopService.getShopByOwner(retailer);
        ShopRegistrationDto shopDto = new ShopRegistrationDto();

        if (shopOpt.isPresent()) {
            Shop shop = shopOpt.get();
            shopDto.setId(shop.getId());
            shopDto.setShopName(shop.getShopName());
            shopDto.setOwnerName(shop.getOwnerName());
            shopDto.setContactNumber(shop.getContactNumber());
            shopDto.setAddress(shop.getAddress());
            shopDto.setCategory(shop.getCategory());
            shopDto.setLocationId(shop.getLocationId());
            shopDto.setOpeningHours(shop.getOpeningHours());
            shopDto.setDescription(shop.getDescription());
        } else {
            shopDto.setOwnerName(retailer.getFullName());
            shopDto.setContactNumber(retailer.getPhone());
            shopDto.setLocationId("LOC-DEMO-1");
            shopDto.setCategory("Consumer Electronics & Accessories");
        }

        model.addAttribute("shopDto", shopDto);
        model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
        model.addAttribute("categories", getAvailableCategories());

        return "dashboard/retailer-shop-form";
    }

    /**
     * Processes shop registration and profile update with strict backend ownership verification.
     */
    @PostMapping("/retailer/shop/save")
    public String saveShop(
            @Valid @ModelAttribute("shopDto") ShopRegistrationDto shopDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User retailer = getAuthenticatedRetailer(userDetails);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", retailer);
            model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
            model.addAttribute("categories", getAvailableCategories());
            return "dashboard/retailer-shop-form";
        }

        try {
            shopService.registerOrUpdateShop(retailer, shopDto);
            redirectAttributes.addFlashAttribute("successMessage", "Store profile successfully updated and synchronized!");
            return "redirect:/retailer/dashboard?saved=true";
        } catch (Exception ex) {
            model.addAttribute("currentUser", retailer);
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
            model.addAttribute("categories", getAvailableCategories());
            return "dashboard/retailer-shop-form";
        }
    }

    // =========================================================================
    // MODULE 5: INVENTORY MANAGEMENT ENDPOINTS
    // =========================================================================

    /**
     * Main Store Inventory Management View:
     * View, search, filter, and monitor in-stock/low-stock/out-of-stock items.
     */
    @GetMapping("/retailer/inventory")
    public String viewInventory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "category", required = false, defaultValue = "all") String category,
            @RequestParam(value = "stockStatus", required = false, defaultValue = "ALL") String stockStatus,
            Model model) {

        User retailer = getAuthenticatedRetailer(userDetails);
        model.addAttribute("currentUser", retailer);

        Optional<Shop> shopOpt = shopService.getShopByOwner(retailer);
        if (shopOpt.isEmpty()) {
            return "redirect:/retailer/dashboard";
        }
        Shop shop = shopOpt.get();
        model.addAttribute("shop", shop);

        // Retrieve inventory with filters
        List<Product> products = inventoryService.getRetailerInventory(retailer, query, category, stockStatus);
        InventoryStatsDto stats = inventoryService.getInventoryStats(retailer);

        model.addAttribute("products", products);
        model.addAttribute("stats", stats);
        model.addAttribute("searchQuery", query != null ? query.trim() : "");
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedStockStatus", stockStatus);
        model.addAttribute("categories", getAvailableCategories());

        return "dashboard/retailer-inventory";
    }

    /**
     * Form to add a new product to store catalog.
     */
    @GetMapping("/retailer/inventory/add")
    public String addProductForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User retailer = getAuthenticatedRetailer(userDetails);
        Optional<Shop> shopOpt = shopService.getShopByOwner(retailer);
        if (shopOpt.isEmpty()) {
            return "redirect:/retailer/dashboard";
        }

        ProductDto productDto = new ProductDto();
        productDto.setCategory(shopOpt.get().getCategory());
        productDto.setQuantity(10);
        productDto.setLowStockThreshold(5);
        productDto.setUnit("pcs");

        model.addAttribute("currentUser", retailer);
        model.addAttribute("shop", shopOpt.get());
        model.addAttribute("productDto", productDto);
        model.addAttribute("categories", getAvailableCategories());
        model.addAttribute("isEdit", false);

        return "dashboard/product-form";
    }

    /**
     * Form to edit an existing product in store catalog.
     */
    @GetMapping("/retailer/inventory/edit/{id}")
    public String editProductForm(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User retailer = getAuthenticatedRetailer(userDetails);
        Optional<Shop> shopOpt = shopService.getShopByOwner(retailer);
        if (shopOpt.isEmpty()) {
            return "redirect:/retailer/dashboard";
        }

        // Retrieves product while strictly enforcing shop ownership in backend
        Product product = inventoryService.getProductForRetailer(retailer, id);

        ProductDto productDto = new ProductDto();
        productDto.setId(product.getId());
        productDto.setName(product.getName());
        productDto.setSku(product.getSku());
        productDto.setCategory(product.getCategory());
        productDto.setPrice(product.getPrice());
        productDto.setOriginalPrice(product.getOriginalPrice());
        productDto.setQuantity(product.getQuantity());
        productDto.setLowStockThreshold(product.getLowStockThreshold());
        productDto.setUnit(product.getUnit());
        productDto.setDescription(product.getDescription());

        model.addAttribute("currentUser", retailer);
        model.addAttribute("shop", shopOpt.get());
        model.addAttribute("productDto", productDto);
        model.addAttribute("categories", getAvailableCategories());
        model.addAttribute("isEdit", true);

        return "dashboard/product-form";
    }

    /**
     * Processes adding or updating a product with non-negative validation and backend ownership checks.
     */
    @PostMapping("/retailer/inventory/save")
    public String saveProduct(
            @Valid @ModelAttribute("productDto") ProductDto productDto,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes) {

        User retailer = getAuthenticatedRetailer(userDetails);
        Optional<Shop> shopOpt = shopService.getShopByOwner(retailer);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", retailer);
            model.addAttribute("shop", shopOpt.orElse(null));
            model.addAttribute("categories", getAvailableCategories());
            model.addAttribute("isEdit", productDto.getId() != null);
            return "dashboard/product-form";
        }

        try {
            if (productDto.getId() == null) {
                inventoryService.addProduct(retailer, productDto);
                redirectAttributes.addFlashAttribute("successMessage", "Product '" + productDto.getName() + "' added to store inventory successfully!");
            } else {
                inventoryService.updateProduct(retailer, productDto.getId(), productDto);
                redirectAttributes.addFlashAttribute("successMessage", "Product '" + productDto.getName() + "' updated successfully!");
            }
            return "redirect:/retailer/inventory";
        } catch (Exception ex) {
            model.addAttribute("currentUser", retailer);
            model.addAttribute("shop", shopOpt.orElse(null));
            model.addAttribute("categories", getAvailableCategories());
            model.addAttribute("isEdit", productDto.getId() != null);
            model.addAttribute("errorMessage", ex.getMessage());
            return "dashboard/product-form";
        }
    }

    /**
     * Deletes a product from the retailer's store catalog after verifying ownership.
     */
    @PostMapping("/retailer/inventory/delete/{id}")
    public String deleteProduct(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User retailer = getAuthenticatedRetailer(userDetails);
        try {
            inventoryService.deleteProduct(retailer, id);
            redirectAttributes.addFlashAttribute("successMessage", "Product removed from store inventory successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/retailer/inventory";
    }

    /**
     * Quick price and stock quantity inline updater.
     */
    @PostMapping("/retailer/inventory/quick-update")
    public String quickUpdate(
            @RequestParam("productId") Long productId,
            @RequestParam("quantity") Integer quantity,
            @RequestParam("price") Double price,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User retailer = getAuthenticatedRetailer(userDetails);
        try {
            inventoryService.quickUpdate(retailer, productId, quantity, price);
            redirectAttributes.addFlashAttribute("successMessage", "Stock & price updated successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/retailer/inventory";
    }

    private User getAuthenticatedRetailer(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Retailer account not found for authenticated principal."));
    }

    private List<String> getAvailableCategories() {
        return List.of(
                "Consumer Electronics & Accessories",
                "Groceries & Daily Essentials",
                "Pharmacy & Healthcare",
                "Fresh Bakery & Confectionery",
                "Stationery & Books",
                "Hardware, Paints & Electricals",
                "Apparel & Fashion"
        );
    }
}
