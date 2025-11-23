package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, UUID> {

    Optional<SubscriptionPayment> findByOrderCode(@Param("orderCode") Long orderCode);

    Optional<SubscriptionPayment> findByUserUserIdAndStatus(@Param("userId") UUID userId,
                                                            @Param("status") SubscriptionPayment.PaymentStatus status);

    @Query("SELECT SUM(sp.amount) FROM SubscriptionPayment sp WHERE sp.status = 'PAID'")
    BigDecimal getTotalRevenue();

    @Query("SELECT COUNT(sp) FROM SubscriptionPayment sp WHERE sp.status = 'PAID'")
    Long getTotalPaidSubscriptions();

    @Query("SELECT COUNT(sp) FROM SubscriptionPayment sp WHERE sp.status = 'PAID' AND sp.plan.name = :planName")
    Long getSubscriptionCountByPlan(@Param("planName") String planName);

    @Query("SELECT SUM(sp.amount) FROM SubscriptionPayment sp WHERE sp.status = 'PAID' AND sp.plan.name = :planName")
    BigDecimal getRevenueByPlan(@Param("planName") String planName);

    @Query("SELECT YEAR(sp.updatedAt) as year, MONTH(sp.updatedAt) as month, SUM(sp.amount) as revenue, COUNT(sp) as count " +
           "FROM SubscriptionPayment sp WHERE sp.status = 'PAID' " +
           "GROUP BY YEAR(sp.updatedAt), MONTH(sp.updatedAt) " +
           "ORDER BY year DESC, month DESC")
    List<Object[]> getMonthlyRevenue();

    @Query("SELECT DATE(sp.updatedAt) as date, SUM(sp.amount) as revenue, COUNT(sp) as count " +
           "FROM SubscriptionPayment sp WHERE sp.status = 'PAID' AND sp.updatedAt >= :startDate " +
           "GROUP BY DATE(sp.updatedAt) " +
           "ORDER BY date DESC")
    List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate);
}