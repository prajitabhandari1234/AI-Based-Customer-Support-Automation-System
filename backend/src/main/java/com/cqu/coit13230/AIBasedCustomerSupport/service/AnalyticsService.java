package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;

/**
 * Service responsible for generating administrator analytics
 * for customer support tickets.
 *
 * <p>
 * Provides aggregated ticket statistics used by the administrator
 * dashboard, including ticket totals and distributions by status,
 * priority, and category.
 * </p>
 */
@Service
public class AnalyticsService {

    private final TicketRepository ticketRepository;

    /**
     * Constructs the analytics service.
     *
     * @param ticketRepository repository used to access ticket data
     */
    public AnalyticsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    /**
     * Generates a summary of current support ticket statistics.
     *
     * @return aggregated ticket analytics
     */
    public AnalyticsSummaryResponse getTicketSummary() {

        AnalyticsSummaryResponse response =
                new AnalyticsSummaryResponse();

        response.setTotalTickets(ticketRepository.count());

        Map<String, Long> statusCounts = new LinkedHashMap<>();

        for (TicketStatus status : TicketStatus.values()) {
            statusCounts.put(
                    status.name(),
                    ticketRepository.countByStatus(status));
        }

        response.setByStatus(statusCounts);

        Map<String, Long> priorityCounts = new LinkedHashMap<>();

        for (TicketPriority priority : TicketPriority.values()) {
            priorityCounts.put(
                    priority.name(),
                    ticketRepository.countByPriority(priority));
        }

        response.setByPriority(priorityCounts);

        Map<String, Long> categoryCounts = new LinkedHashMap<>();

        for (TicketCategory category : TicketCategory.values()) {
            categoryCounts.put(
                    category.name(),
                    ticketRepository.countByCategory(category));
        }

        response.setByCategory(categoryCounts);

        response.setEscalatedTickets(
                ticketRepository.countByStatus(
                        TicketStatus.ESCALATED));

        response.setResolvedTickets(
                ticketRepository.countByStatus(
                        TicketStatus.RESOLVED));

        return response;
    }
}