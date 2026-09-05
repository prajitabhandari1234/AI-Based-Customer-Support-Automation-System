package com.cqu.coit13230.AIBasedCustomerSupport.analytics;

import java.util.Map;

public record AnalyticsSummary(
        long totalTickets,
        long openTickets,
        long resolvedTickets,
        long escalatedTickets,
        double escalationRate,
        double chatbotSuccessRate,
        double averageFirstResponseSeconds,
        double averageResolutionMinutes,
        Map<String, Long> ticketsByStatus,
        Map<String, Long> ticketsByCategory,
        Map<String, Long> ticketsByPriority,
        Map<String, Long> sentimentDistribution,
        Map<String, Long> dailyTicketVolume
) {}
