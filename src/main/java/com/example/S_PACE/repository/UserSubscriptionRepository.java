package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UUID> {

    @Query("SELECT us FROM UserSubscription us WHERE us.user.userId = :userId ORDER BY us.purchasedAt DESC")
    Optional<UserSubscription> findTopByUserIdOrderByPurchasedAtDesc(UUID userId);
}
