package com.example.S_PACE.service.impl;

import com.example.S_PACE.dto.response.RevenueReportResponse;
import com.example.S_PACE.repository.SubscriptionPaymentRepository;
import com.example.S_PACE.service.RevenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RevenueServiceImpl implements RevenueService {

    private static final Logger logger = LoggerFactory.getLogger(RevenueServiceImpl.class);

    @Autowired
    private SubscriptionPaymentRepository subscriptionPaymentRepository;

    @Override
    public RevenueReportResponse getRevenueReport() {
        logger.info("Generating complete revenue report");

        BigDecimal totalRevenue = subscriptionPaymentRepository.getTotalRevenue();
        Long totalSubscriptions = subscriptionPaymentRepository.getTotalPaidSubscriptions();

        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
        if (totalSubscriptions == null) totalSubscriptions = 0L;

        RevenueReportResponse.PlanStatistics planStats = getPlanStatistics();
        List<RevenueReportResponse.MonthlyRevenue> monthlyRevenue = getMonthlyRevenue();
        List<RevenueReportResponse.DailyRevenue> dailyRevenue = getDailyRevenue(LocalDate.now().minusDays(30));

        return RevenueReportResponse.builder()
                .totalRevenue(totalRevenue)
                .totalSubscriptions(totalSubscriptions)
                .planStatistics(planStats)
                .monthlyRevenue(monthlyRevenue)
                .dailyRevenue(dailyRevenue)
                .build();
    }

    @Override
    public RevenueReportResponse.PlanStatistics getPlanStatistics() {
        logger.info("Generating plan statistics");

        // Basic Plan
        Long basicCount = subscriptionPaymentRepository.getSubscriptionCountByPlan("Basic");
        BigDecimal basicRevenue = subscriptionPaymentRepository.getRevenueByPlan("Basic");

        // Premium Plan
        Long premiumCount = subscriptionPaymentRepository.getSubscriptionCountByPlan("Premium");
        BigDecimal premiumRevenue = subscriptionPaymentRepository.getRevenueByPlan("Premium");

        // Enterprise Plan
        Long enterpriseCount = subscriptionPaymentRepository.getSubscriptionCountByPlan("Enterprise");
        BigDecimal enterpriseRevenue = subscriptionPaymentRepository.getRevenueByPlan("Enterprise");

        // Handle nulls
        if (basicCount == null) basicCount = 0L;
        if (basicRevenue == null) basicRevenue = BigDecimal.ZERO;
        if (premiumCount == null) premiumCount = 0L;
        if (premiumRevenue == null) premiumRevenue = BigDecimal.ZERO;
        if (enterpriseCount == null) enterpriseCount = 0L;
        if (enterpriseRevenue == null) enterpriseRevenue = BigDecimal.ZERO;

        return RevenueReportResponse.PlanStatistics.builder()
                .basicPlanCount(basicCount)
                .basicPlanRevenue(basicRevenue)
                .premiumPlanCount(premiumCount)
                .premiumPlanRevenue(premiumRevenue)
                .enterprisePlanCount(enterpriseCount)
                .enterprisePlanRevenue(enterpriseRevenue)
                .build();
    }

    @Override
    public List<RevenueReportResponse.MonthlyRevenue> getMonthlyRevenue() {
        logger.info("Generating monthly revenue report");

        List<Object[]> monthlyData = subscriptionPaymentRepository.getMonthlyRevenue();

        return monthlyData.stream()
                .map(row -> RevenueReportResponse.MonthlyRevenue.builder()
                        .year(((Number) row[0]).intValue())
                        .month(((Number) row[1]).intValue())
                        .revenue((BigDecimal) row[2])
                        .subscriptionCount(((Number) row[3]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueReportResponse.DailyRevenue> getDailyRevenue(LocalDate startDate) {
        logger.info("Generating daily revenue report from: {}", startDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        List<Object[]> dailyData = subscriptionPaymentRepository.getDailyRevenue(startDateTime);

        return dailyData.stream()
                .map(row -> {
                    LocalDate date;
                    if (row[0] instanceof Date) {
                        date = ((Date) row[0]).toLocalDate();
                    } else {
                        date = (LocalDate) row[0];
                    }

                    return RevenueReportResponse.DailyRevenue.builder()
                            .date(date)
                            .revenue((BigDecimal) row[1])
                            .subscriptionCount(((Number) row[2]).longValue())
                            .build();
                })
                .collect(Collectors.toList());
    }
}