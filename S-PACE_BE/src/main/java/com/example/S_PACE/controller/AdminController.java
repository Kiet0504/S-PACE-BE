package com.example.S_PACE.controller;

import com.example.S_PACE.dto.response.ResponseDTO;
import com.example.S_PACE.dto.response.RevenueReportResponse;
import com.example.S_PACE.service.RevenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Management", description = "Administrative functions including revenue reporting")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private RevenueService revenueService;

    @GetMapping("/revenue/report")
    @Operation(summary = "Get complete revenue report", description = "Get comprehensive revenue analytics including total revenue, plan statistics, and time-based analysis")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue report retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<RevenueReportResponse>> getRevenueReport() {
        try {
            logger.info("Admin requesting complete revenue report");
            RevenueReportResponse report = revenueService.getRevenueReport();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Revenue report retrieved successfully", report));
        } catch (Exception ex) {
            logger.error("Error generating revenue report: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                .body(new ResponseDTO<>(false, "Failed to generate revenue report", null));
        }
    }

    @GetMapping("/revenue/total")
    @Operation(summary = "Get total revenue", description = "Get total revenue from all paid subscriptions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Total revenue retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<RevenueReportResponse>> getTotalRevenue() {
        try {
            logger.info("Admin requesting total revenue");
            RevenueReportResponse report = revenueService.getRevenueReport();

            // Create simplified response with just total data
            RevenueReportResponse totalOnly = RevenueReportResponse.builder()
                    .totalRevenue(report.getTotalRevenue())
                    .totalSubscriptions(report.getTotalSubscriptions())
                    .build();

            return ResponseEntity.ok(new ResponseDTO<>(true, "Total revenue retrieved successfully", totalOnly));
        } catch (Exception ex) {
            logger.error("Error getting total revenue: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                .body(new ResponseDTO<>(false, "Failed to get total revenue", null));
        }
    }

    @GetMapping("/revenue/plans")
    @Operation(summary = "Get plan statistics", description = "Get revenue and subscription count breakdown by plan type (Basic, Premium, Enterprise)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Plan statistics retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<RevenueReportResponse.PlanStatistics>> getPlanStatistics() {
        try {
            logger.info("Admin requesting plan statistics");
            RevenueReportResponse.PlanStatistics planStats = revenueService.getPlanStatistics();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Plan statistics retrieved successfully", planStats));
        } catch (Exception ex) {
            logger.error("Error getting plan statistics: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                .body(new ResponseDTO<>(false, "Failed to get plan statistics", null));
        }
    }

    @GetMapping("/revenue/monthly")
    @Operation(summary = "Get monthly revenue", description = "Get revenue breakdown by month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Monthly revenue retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<RevenueReportResponse.MonthlyRevenue>>> getMonthlyRevenue() {
        try {
            logger.info("Admin requesting monthly revenue");
            List<RevenueReportResponse.MonthlyRevenue> monthlyRevenue = revenueService.getMonthlyRevenue();
            return ResponseEntity.ok(new ResponseDTO<>(true, "Monthly revenue retrieved successfully", monthlyRevenue));
        } catch (Exception ex) {
            logger.error("Error getting monthly revenue: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                .body(new ResponseDTO<>(false, "Failed to get monthly revenue", null));
        }
    }

    @GetMapping("/revenue/daily")
    @Operation(summary = "Get daily revenue", description = "Get revenue breakdown by day from a specified start date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daily revenue retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date format"),
        @ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ResponseDTO<List<RevenueReportResponse.DailyRevenue>>> getDailyRevenue(
            @Parameter(description = "Start date for daily revenue report (YYYY-MM-DD). Defaults to 30 days ago if not provided.")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        try {
            if (startDate == null) {
                startDate = LocalDate.now().minusDays(30);
            }

            logger.info("Admin requesting daily revenue from: {}", startDate);
            List<RevenueReportResponse.DailyRevenue> dailyRevenue = revenueService.getDailyRevenue(startDate);
            return ResponseEntity.ok(new ResponseDTO<>(true, "Daily revenue retrieved successfully", dailyRevenue));
        } catch (Exception ex) {
            logger.error("Error getting daily revenue: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500)
                .body(new ResponseDTO<>(false, "Failed to get daily revenue", null));
        }
    }
}