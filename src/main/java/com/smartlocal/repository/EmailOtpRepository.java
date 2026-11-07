package com.smartlocal.repository;

import com.smartlocal.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing EmailOtp verification records.
 */
@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE EmailOtp o SET o.used = true WHERE o.email = :email AND o.used = false")
    void invalidateAllActiveForEmail(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM EmailOtp o WHERE o.expiresAt < :cutoff")
    void deleteExpiredBefore(@Param("cutoff") LocalDateTime cutoff);
}

