package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the main ticket metrics used by the analytics page.
 *
 * <p>
 * This Data Transfer Object (DTO) contains summary information used
 * to present ticket and customer support performance metrics within
 * the application's analytics functionality.
 * </p>
 *
 * <p>
 * The response includes ticket totals, enquiry counts, escalation
 * information, chatbot performance, response and resolution times,
 * ticket distributions, sentiment distribution, and daily ticket volume.
 * The DTO keeps analytics API response data separate from the database
 * entities.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {

    /**
     * Total number of tickets included in the analytics summary.
     */
    private long totalTickets;

    /**
     * Total number of customer enquiries included in the analytics data.
     */
    private long enquiryCount;

    /**
     * Number of tickets currently considered open.
     */
    private long openTickets;

    /**
     * Number of tickets that have been resolved.
     */
    private long resolvedTickets;

    /**
     * Number of tickets that have been escalated.
     */
    private long escalatedTickets;

    /**
     * Escalation rate represented as a percentage.
     */
    private double escalationRatePercent;

    /**
     * Escalation rate calculated for the ticket data included
     * in the analytics summary.
     */
    private double escalationRate;

    /**
     * Success rate of chatbot interactions represented in the
     * analytics data.
     */
    private double chatbotSuccessRate;

    /**
     * Average response time measured in hours.
     *
     * <p>
     * This value may be {@code null} when an average response time
     * cannot be calculated from the available data.
     * </p>
     */
    private Double averageResponseTimeHours;

    /**
     * Average resolution time measured in hours.
     *
     * <p>
     * This value may be {@code null} when an average resolution time
     * cannot be calculated from the available data.
     * </p>
     */
    private Double averageResolutionTimeHours;

    /**
     * Average time taken to provide the first response,
     * measured in seconds.
     */
    private double averageFirstResponseSeconds;

    /**
     * Average time taken to resolve tickets, measured in minutes.
     */
    private double averageResolutionMinutes;

    /**
     * Number of tickets grouped by ticket status.
     *
     * <p>
     * This field is returned in the JSON response using the property
     * name {@code ticketsByStatus}.
     * </p>
     */
    @JsonProperty("ticketsByStatus")
    private Map<String, Long> byStatus = new LinkedHashMap<>();

    /**
     * Number of tickets grouped by ticket priority.
     *
     * <p>
     * This field is returned in the JSON response using the property
     * name {@code ticketsByPriority}.
     * </p>
     */
    @JsonProperty("ticketsByPriority")
    private Map<String, Long> byPriority = new LinkedHashMap<>();

    /**
     * Number of tickets grouped by ticket category.
     *
     * <p>
     * This field is returned in the JSON response using the property
     * name {@code ticketsByCategory}.
     * </p>
     */
    @JsonProperty("ticketsByCategory")
    private Map<String, Long> byCategory = new LinkedHashMap<>();

    /**
     * Number of records grouped by their sentiment classification.
     */
    private Map<String, Long> sentimentDistribution = new LinkedHashMap<>();

    /**
     * Number of tickets grouped by date to represent daily
     * ticket volume.
     */
    private Map<String, Long> dailyTicketVolume = new LinkedHashMap<>();

}