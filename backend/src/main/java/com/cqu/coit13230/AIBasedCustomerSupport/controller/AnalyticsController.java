package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsReportResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.service.AnalyticsService;

/**
 * Provides admin endpoints for ticket analytics and reports.
 * The controller passes filter and reporting parameters to the analytics service.
 */
@RestController
@RequestMapping("/api")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/analytics/summary")
    public ResponseEntity<AnalyticsSummaryResponse> getSummary() {
        return ResponseEntity.ok(analyticsService.getTicketSummary());
    }

    @GetMapping("/admin/analytics/tickets")
    public ResponseEntity<AnalyticsSummaryResponse> getTicketSummary() {
        return ResponseEntity.ok(analyticsService.getTicketSummary());
    }

    // All filter values are optional so the same endpoint can support different dashboard searches.
    @GetMapping("/admin/analytics/tickets/filter")
    public ResponseEntity<AnalyticsSummaryResponse> getFilteredTicketSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Double minSentiment,
            @RequestParam(required = false) Double maxSentiment) {

        return ResponseEntity.ok(
                analyticsService.getFilteredTicketSummary(
                        startDate,
                        endDate,
                        category,
                        priority,
                        status,
                        minSentiment,
                        maxSentiment));
    }

    @GetMapping("/admin/analytics/reports/weekly")
    public ResponseEntity<AnalyticsReportResponse> getWeeklyReport(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(analyticsService.getWeeklyReport(date));
    }

    @GetMapping("/admin/analytics/reports/monthly")
    public ResponseEntity<AnalyticsReportResponse> getMonthlyReport(
            @RequestParam LocalDate date) {

        return ResponseEntity.ok(analyticsService.getMonthlyReport(date));
    }
}
