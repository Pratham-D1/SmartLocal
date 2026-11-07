package com.smartlocal.repository;

import com.smartlocal.entity.Product;
import com.smartlocal.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Product entity.
 * Includes query methods enforcing shop-level data isolation.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByShopOrderByCreatedAtDesc(Shop shop);

    Optional<Product> findByIdAndShop(Long id, Shop shop);

    long countByShop(Shop shop);

    long countByQuantityGreaterThan(Integer quantity);

    long countByQuantity(Integer quantity);

    @Query("SELECT p FROM Product p WHERE p.shop = :shop " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:category IS NULL OR :category = 'all' OR p.category = :category) " +
           "ORDER BY p.createdAt DESC")
    List<Product> searchInventory(
            @Param("shop") Shop shop,
            @Param("keyword") String keyword,
            @Param("category") String category);

    // Queries supporting customer discovery and location-based searches
    List<Product> findByShop_LocationId(String locationId);

    @Query("SELECT p FROM Product p JOIN FETCH p.shop WHERE " +
           "(:locationId IS NULL OR :locationId = 'all' OR p.shop.locationId = :locationId) AND " +
           "(:category IS NULL OR :category = 'all' OR p.category = :category) AND " +
           "(:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.shop.shopName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Product> searchForCustomers(
            @Param("locationId") String locationId,
            @Param("category") String category,
            @Param("keyword") String keyword);
}
