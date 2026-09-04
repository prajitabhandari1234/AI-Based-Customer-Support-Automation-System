package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
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
 *
 * <p>
 * The service also supports dynamic analytics filtering by date range,
 * category, priority, status, and sentiment score.
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

    /**
     * Generates filtered ticket analytics using optional filter criteria.
     *
     * <p>
     * Any filter value may be {@code null}. When a filter is not supplied,
     * it is excluded from the database query.
     * </p>
     *
     * @param startDate minimum ticket creation date
     * @param endDate maximum ticket creation date
     * @param category ticket category to include
     * @param priority ticket priority to include
     * @param status ticket lifecycle status to include
     * @param minSentiment minimum sentiment score
     * @param maxSentiment maximum sentiment score
     * @return analytics summary matching the supplied filters
     */
    public AnalyticsSummaryResponse getFilteredTicketSummary(
            LocalDate startDate,
            LocalDate endDate,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            Double minSentiment,
            Double maxSentiment) {

        validateFilters(
                startDate,
                endDate,
                minSentiment,
                maxSentiment);

        Specification<Ticket> specification =
                Specification.unrestricted();

        if (startDate != null) {

            LocalDateTime startDateTime =
                    startDate.atStartOfDay();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("createdAt"),
                                    startDateTime));
        }

        if (endDate != null) {

            LocalDateTime endDateTime =
                    endDate.plusDays(1).atStartOfDay();

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThan(
                                    root.get("createdAt"),
                                    endDateTime));
        }

        if (category != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("category"),
                                    category));
        }

        if (priority != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("priority"),
                                    priority));
        }

        if (status != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(
                                    root.get("status"),
                                    status));
        }

        if (minSentiment != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(
                                    root.<Double>get("sentimentScore"),
                                    minSentiment));
        }

        if (maxSentiment != null) {
            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.<Double>get("sentimentScore"),
                                    maxSentiment));
        }

        List<Ticket> tickets =
                ticketRepository.findAll(specification);

        return buildAnalyticsSummary(tickets);
    }

    /**
     * Validates analytics filter values before querying the database.
     *
     * @param startDate minimum creation date
     * @param endDate maximum creation date
     * @param minSentiment minimum sentiment score
     * @param maxSentiment maximum sentiment score
     */
    private void validateFilters(
            LocalDate startDate,
            LocalDate endDate,
            Double minSentiment,
            Double maxSentiment) {

        if (startDate != null
                && endDate != null
                && startDate.isAfter(endDate)) {

            throw new IllegalArgumentException(
                    "Start date cannot be after end date");
        }

        if (minSentiment != null
                && (minSentiment < -1.0 || minSentiment > 1.0)) {

            throw new IllegalArgumentException(
                    "Minimum sentiment score must be between -1.0 and 1.0");
        }

        if (maxSentiment != null
                && (maxSentiment < -1.0 || maxSentiment > 1.0)) {

            throw new IllegalArgumentException(
                    "Maximum sentiment score must be between -1.0 and 1.0");
        }

        if (minSentiment != null
                && maxSentiment != null
                && minSentiment > maxSentiment) {

            throw new IllegalArgumentException(
                    "Minimum sentiment score cannot be greater than maximum sentiment score");
        }
    }

    /**
     * Builds an analytics summary from the supplied list of tickets.
     *
     * @param tickets tickets included in the analytics calculation
     * @return aggregated analytics summary
     */
    private AnalyticsSummaryResponse buildAnalyticsSummary(
            List<Ticket> tickets) {

        AnalyticsSummaryResponse response =
                new AnalyticsSummaryResponse();

        response.setTotalTickets(tickets.size());

        Map<String, Long> statusCounts =
                new LinkedHashMap<>();

        for (TicketStatus status : TicketStatus.values()) {

            long count = tickets.stream()
                    .filter(ticket ->
                            ticket.getStatus() == status)
                    .count();

            statusCounts.put(
                    status.name(),
                    count);
        }

        response.setByStatus(statusCounts);

        Map<String, Long> priorityCounts =
                new LinkedHashMap<>();

        for (TicketPriority priority : TicketPriority.values()) {

            long count = tickets.stream()
                    .filter(ticket ->
                            ticket.getPriority() == priority)
                    .count();

            priorityCounts.put(
                    priority.name(),
                    count);
        }

        response.setByPriority(priorityCounts);

        Map<String, Long> categoryCounts =
                new LinkedHashMap<>();

        for (TicketCategory category : TicketCategory.values()) {

            long count = tickets.stream()
                    .filter(ticket ->
                            ticket.getCategory() == category)
                    .count();

            categoryCounts.put(
                    category.name(),
                    count);
        }

        response.setByCategory(categoryCounts);

        long escalatedTickets = tickets.stream()
                .filter(ticket ->
                        ticket.getStatus()
                                == TicketStatus.ESCALATED)
                .count();

        response.setEscalatedTickets(
                escalatedTickets);

        long resolvedTickets = tickets.stream()
                .filter(ticket ->
                        ticket.getStatus()
                                == TicketStatus.RESOLVED)
                .count();

        response.setResolvedTickets(
                resolvedTickets);

        return response;
    }
}