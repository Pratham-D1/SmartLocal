package com.smartlocal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for SmartLocal Application.
 * SmartLocal - AI-Based Hyperlocal Product Discovery and Inventory Management System.
 */
@SpringBootApplication
public class SmartLocalApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartLocalApplication.class, args);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.CommandLineRunner initModule6Data(com.smartlocal.service.ShopDiscoveryService shopDiscoveryService) {
        return args -> {
            shopDiscoveryService.initializeDemoLocationsAndShops();
        };
    }
}
