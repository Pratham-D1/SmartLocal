package com.smartlocal.controller;

import com.smartlocal.service.CustomerProductService;
import com.smartlocal.service.ShopDiscoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling public landing page and shop browsing showcase.
 */
@Controller
public class HomeController {

    private final ShopDiscoveryService shopDiscoveryService;
    private final CustomerProductService customerProductService;

    @Autowired
    public HomeController(ShopDiscoveryService shopDiscoveryService,
                          CustomerProductService customerProductService) {
        this.shopDiscoveryService = shopDiscoveryService;
        this.customerProductService = customerProductService;
    }

    /**
     * Homepage displaying project branding, hero section, search bar,
     * location selectors, featured nearby shops, and real product previews.
     */
    @GetMapping("/")
    public String index(
            @RequestParam(value = "location", required = false) String selectedLocationId,
            @RequestParam(value = "query", required = false) String searchQuery,
            Model model) {

        model.addAttribute("appName", "SmartLocal");
        model.addAttribute("tagline", "Hyperlocal Product Discovery & Inventory System");
        model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
        model.addAttribute("shops", shopDiscoveryService.getFeaturedDemoShops());
        model.addAttribute("categories", customerProductService.getCategories());
        model.addAttribute("featuredProducts", customerProductService.searchProducts(null, "all", "ALL", null, null, null, "nearest"));
        model.addAttribute("selectedLocationId", selectedLocationId);
        model.addAttribute("searchQuery", searchQuery);

        return "index";
    }

    /**
     * Explore Shops navigation route.
     */
    @GetMapping("/explore")
    public String explore(Model model) {
        model.addAttribute("appName", "SmartLocal");
        model.addAttribute("locations", shopDiscoveryService.getDemoLocations());
        model.addAttribute("shops", shopDiscoveryService.getFeaturedDemoShops());
        model.addAttribute("categories", customerProductService.getCategories());
        model.addAttribute("featuredProducts", customerProductService.searchProducts(null, "all", "ALL", null, null, null, "nearest"));
        return "index";
    }
}
