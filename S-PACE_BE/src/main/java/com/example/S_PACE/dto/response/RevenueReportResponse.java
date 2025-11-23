package com.example.S_PACE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReportResponse {
    private BigDecimal totalRevenue;
    private Long totalSubscriptions;
    private PlanStatistics planStatistics;
    private List<MonthlyRevenue> monthlyRevenue;
    private List<DailyRevenue> dailyRevenue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanStatistics {
        private Long basicPlanCount;
        private BigDecimal basicPlanRevenue;
        private Long premiumPlanCount;
        private BigDecimal premiumPlanRevenue;
        private Long enterprisePlanCount;
        private BigDecimal enterprisePlanRevenue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MonthlyRevenue {
        private int year;
        private int month;
        private BigDecimal revenue;
        private Long subscriptionCount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenue {
        private LocalDate date;
        private BigDecimal revenue;
        private Long subscriptionCount;
    }
}