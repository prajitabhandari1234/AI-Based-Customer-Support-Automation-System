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
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {

    private long totalTickets;
    private long enquiryCount;
    private long openTickets;
    private long resolvedTickets;
    private long escalatedTickets;
    private double escalationRatePercent;
    private double escalationRate;
    private double chatbotSuccessRate;
    private Double averageResponseTimeHours;
    private Double averageResolutionTimeHours;
    private double averageFirstResponseSeconds;
    private double averageResolutionMinutes;

    @JsonProperty("ticketsByStatus")
    private Map<String, Long> byStatus = new LinkedHashMap<>();

    @JsonProperty("ticketsByPriority")
    private Map<String, Long> byPriority = new LinkedHashMap<>();

    @JsonProperty("ticketsByCategory")
    private Map<String, Long> byCategory = new LinkedHashMap<>();

    private Map<String, Long> sentimentDistribution = new LinkedHashMap<>();
    private Map<String, Long> dailyTicketVolume = new LinkedHashMap<>();
}
