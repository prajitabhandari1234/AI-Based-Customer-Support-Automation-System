package com.cqu.coit13230.AIBasedCustomerSupport.controller;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
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

    /**
     * Retrieves filtered support-ticket analytics.
     *
     * <p>
     * All filter parameters are optional. Filters may be used
     * individually or combined.
     * </p>
     *
     * @param startDate minimum ticket creation date
     * @param endDate maximum ticket creation date
     * @param category ticket category
     * @param priority ticket priority
     * @param status ticket lifecycle status
     * @param minSentiment minimum sentiment score
     * @param maxSentiment maximum sentiment score
     * @return analytics summary matching the supplied filters
     */
    @GetMapping("/tickets/filter")
    public ResponseEntity<AnalyticsSummaryResponse> getFilteredTicketSummary(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Double minSentiment,
            @RequestParam(required = false) Double maxSentiment) {

        AnalyticsSummaryResponse summary =
                analyticsService.getFilteredTicketSummary(
                        startDate,
                        endDate,
                        category,
                        priority,
                        status,
                        minSentiment,
                        maxSentiment);

        return ResponseEntity.ok(summary);
    }
}