package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.service.AnalyticsService;

/**
 * REST controller for administrator analytics operations.
 *
 * <p>
 * Provides aggregated support-ticket statistics for use by
 * the administrator dashboard.
 * </p>
 *
 * <p>
 * Access to these endpoints is restricted to users with the
 * ADMIN role through the application's security configuration.
 * </p>
 */
@RestController
@RequestMapping("/api/admin/analytics")
public class AdminAnalyticsController {

    private final AnalyticsService analyticsService;

    /**
     * Constructs the administrator analytics controller.
     *
     * @param analyticsService service used to generate analytics data
     */
    public AdminAnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Retrieves summary statistics for all support tickets.
     *
     * @return response containing aggregated ticket analytics
     */
    @GetMapping("/tickets")
    public ResponseEntity<AnalyticsSummaryResponse> getTicketSummary() {

        AnalyticsSummaryResponse summary =
                analyticsService.getTicketSummary();

        return ResponseEntity.ok(summary);
    }
}