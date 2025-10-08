package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, UUID> {

    Optional<SubscriptionPayment> findByOrderCode(@Param("orderCode") Long orderCode);

    Optional<SubscriptionPayment> findByUserUserIdAndStatus(@Param("userId") UUID userId,
                                                            @Param("status") SubscriptionPayment.PaymentStatus status);
}