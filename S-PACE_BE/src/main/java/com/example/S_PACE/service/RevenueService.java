package com.example.S_PACE.service;

import com.example.S_PACE.dto.response.RevenueReportResponse;

import java.time.LocalDate;

public interface RevenueService {
    RevenueReportResponse getRevenueReport();
    RevenueReportResponse.PlanStatistics getPlanStatistics();
    java.util.List<RevenueReportResponse.MonthlyRevenue> getMonthlyRevenue();
    java.util.List<RevenueReportResponse.DailyRevenue> getDailyRevenue(LocalDate startDate);
}