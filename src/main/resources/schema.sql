-- =====================================================================
-- SmartLocal - MySQL Database Schema Script
-- Target Database: smartlocal_db
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `smartlocal_db`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `smartlocal_db`;

-- ---------------------------------------------------------------------
-- 1. Table: users
-- Stores Customer, Retailer, and Administrator user credentials
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `full_name` VARCHAR(100) NOT NULL,
    `email` VARCHAR(120) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `phone` VARCHAR(20) DEFAULT NULL,
    `role` VARCHAR(30) NOT NULL,
    `enabled` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_users_email` UNIQUE (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 2. Table: shops
-- Stores retail stores, linked 1-to-1 with a Retailer user
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `shops` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `shop_name` VARCHAR(120) NOT NULL,
    `owner_name` VARCHAR(100) NOT NULL,
    `contact_number` VARCHAR(20) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `category` VARCHAR(80) NOT NULL,
    `location_id` VARCHAR(50) NOT NULL,
    `location_name` VARCHAR(120) NOT NULL,
    `description` VARCHAR(500) DEFAULT NULL,
    `opening_hours` VARCHAR(60) DEFAULT '09:00 AM - 09:00 PM',
    `approval_status` VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `latitude` DOUBLE DEFAULT NULL,
    `longitude` DOUBLE DEFAULT NULL,
    `owner_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `uk_shops_owner` UNIQUE (`owner_id`),
    CONSTRAINT `fk_shops_owner` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 3. Table: products
-- Stores inventory products linked to their corresponding retail shop
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(150) NOT NULL,
    `sku` VARCHAR(60) DEFAULT NULL,
    `category` VARCHAR(80) NOT NULL,
    `price` DOUBLE NOT NULL,
    `original_price` DOUBLE DEFAULT NULL,
    `quantity` INT NOT NULL DEFAULT 0,
    `low_stock_threshold` INT NOT NULL DEFAULT 5,
    `unit` VARCHAR(30) DEFAULT 'pcs',
    `description` VARCHAR(1000) DEFAULT NULL,
    `shop_id` BIGINT NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_products_shop` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Optional indexes for search optimization
CREATE INDEX `idx_products_category` ON `products` (`category`);
CREATE INDEX `idx_shops_location` ON `shops` (`location_id`);
