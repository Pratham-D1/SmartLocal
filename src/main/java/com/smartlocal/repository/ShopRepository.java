package com.smartlocal.repository;

import com.smartlocal.entity.ApprovalStatus;
import com.smartlocal.entity.Shop;
import com.smartlocal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for Shop entity.
 */
@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findByOwner(User owner);

    Optional<Shop> findByOwnerId(Long ownerId);

    boolean existsByOwner(User owner);

    List<Shop> findByApprovalStatus(ApprovalStatus status);

    List<Shop> findByLocationId(String locationId);

    List<Shop> findByActive(boolean active);

    long countByApprovalStatus(ApprovalStatus status);

    long countByActive(boolean active);
}
