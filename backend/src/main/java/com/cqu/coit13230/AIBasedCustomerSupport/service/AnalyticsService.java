package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsReportResponse;
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
 * priority, category, escalation rate, and average resolution time.
 * </p>
 *
 * <p>
 * The service supports dynamic analytics filtering by date range,
 * category, priority, status, and sentiment score. It also generates
 * weekly and monthly summary reports for administrator reporting.
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
    public AnalyticsService(
            TicketRepository ticketRepository) {

        this.ticketRepository = ticketRepository;
    }

    /**
     * Generates a summary of current support ticket statistics.
     *
     * <p>
     * Historical escalation information is determined using
     * the escalatedAt timestamp rather than the current ticket status.
     * Average resolution time is calculated using createdAt and
     * resolvedAt timestamps.
     * </p>
     *
     * @return aggregated ticket analytics
     */
    public AnalyticsSummaryResponse getTicketSummary() {

        List<Ticket> tickets =
                ticketRepository.findAll();

        return buildAnalyticsSummary(tickets);
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
                                    root.<Double>get(
                                            "sentimentScore"),
                                    minSentiment));
        }

        if (maxSentiment != null) {

            specification = specification.and(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(
                                    root.<Double>get(
                                            "sentimentScore"),
                                    maxSentiment));
        }

        List<Ticket> tickets =
                ticketRepository.findAll(specification);

        return buildAnalyticsSummary(tickets);
    }

    /**
     * Generates a weekly analytics report for the week containing
     * the specified reference date.
     *
     * <p>
     * The reporting week begins on Monday and ends on Sunday.
     * </p>
     *
     * @param date reference date used to determine the reporting week
     * @return weekly ticket analytics report
     * @throws IllegalArgumentException if the reference date is missing
     */
    public AnalyticsReportResponse getWeeklyReport(
            LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Reference date is required");
        }

        LocalDate startDate =
                date.with(
                        TemporalAdjusters.previousOrSame(
                                DayOfWeek.MONDAY));

        LocalDate endDate =
                date.with(
                        TemporalAdjusters.nextOrSame(
                                DayOfWeek.SUNDAY));

        return buildAnalyticsReport(
                "WEEKLY",
                startDate,
                endDate);
    }

    /**
     * Generates a monthly analytics report for the month containing
     * the specified reference date.
     *
     * <p>
     * The reporting period begins on the first day of the month
     * and ends on the final day of the same month.
     * </p>
     *
     * @param date reference date used to determine the reporting month
     * @return monthly ticket analytics report
     * @throws IllegalArgumentException if the reference date is missing
     */
    public AnalyticsReportResponse getMonthlyReport(
            LocalDate date) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "Reference date is required");
        }

        LocalDate startDate =
                date.withDayOfMonth(1);

        LocalDate endDate =
                date.withDayOfMonth(
                        date.lengthOfMonth());

        return buildAnalyticsReport(
                "MONTHLY",
                startDate,
                endDate);
    }

    /**
     * Builds a periodic analytics report for the specified date range.
     *
     * @param reportType type of report being generated
     * @param startDate beginning of the reporting period
     * @param endDate end of the reporting period
     * @return completed analytics report
     */
    private AnalyticsReportResponse buildAnalyticsReport(
            String reportType,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.plusDays(1).atStartOfDay();

        Specification<Ticket> specification =
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.and(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("createdAt"),
                                        startDateTime),
                                criteriaBuilder.lessThan(
                                        root.get("createdAt"),
                                        endDateTime));

        List<Ticket> tickets =
                ticketRepository.findAll(specification);

        AnalyticsSummaryResponse summary =
                buildAnalyticsSummary(tickets);

        AnalyticsReportResponse report =
                new AnalyticsReportResponse();

        report.setReportType(reportType);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());
        report.setSummary(summary);

        return report;
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
                && (minSentiment < -1.0
                || minSentiment > 1.0)) {

            throw new IllegalArgumentException(
                    "Minimum sentiment score must be between -1.0 and 1.0");
        }

        if (maxSentiment != null
                && (maxSentiment < -1.0
                || maxSentiment > 1.0)) {

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
     * <p>
     * Historical escalation is determined using escalatedAt rather
     * than the current status. Average resolution time is calculated
     * only for tickets containing both createdAt and resolvedAt.
     * </p>
     *
     * @param tickets tickets included in the analytics calculation
     * @return aggregated analytics summary
     */
    private AnalyticsSummaryResponse buildAnalyticsSummary(
            List<Ticket> tickets) {

        AnalyticsSummaryResponse response =
                new AnalyticsSummaryResponse();

        long totalTickets = tickets.size();

        response.setTotalTickets(totalTickets);

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

        for (TicketPriority priority :
                TicketPriority.values()) {

            long count = tickets.stream()
                    .filter(ticket ->
                            ticket.getPriority()
                                    == priority)
                    .count();

            priorityCounts.put(
                    priority.name(),
                    count);
        }

        response.setByPriority(priorityCounts);

        Map<String, Long> categoryCounts =
                new LinkedHashMap<>();

        for (TicketCategory category :
                TicketCategory.values()) {

            long count = tickets.stream()
                    .filter(ticket ->
                            ticket.getCategory()
                                    == category)
                    .count();

            categoryCounts.put(
                    category.name(),
                    count);
        }

        response.setByCategory(categoryCounts);

        /*
         * Count tickets that have ever been escalated.
         *
         * The current ticket status must not be used because an
         * escalated ticket may later become IN_PROGRESS, RESOLVED,
         * or CLOSED.
         */
        long escalatedTickets = tickets.stream()
                .filter(ticket ->
                        ticket.getEscalatedAt() != null)
                .count();

        response.setEscalatedTickets(
                escalatedTickets);

        /*
         * Calculate escalation rate as:
         *
         * historically escalated tickets / total tickets * 100
         */
        double escalationRatePercent = 0.0;

        if (totalTickets > 0) {

            escalationRatePercent =
                    ((double) escalatedTickets
                            / totalTickets) * 100.0;
        }

        response.setEscalationRatePercent(
                roundToTwoDecimalPlaces(
                        escalationRatePercent));

        /*
         * Maintain the existing resolved ticket count based on the
         * current RESOLVED lifecycle status.
         */
        long resolvedTickets = tickets.stream()
                .filter(ticket ->
                        ticket.getStatus()
                                == TicketStatus.RESOLVED)
                .count();

        response.setResolvedTickets(
                resolvedTickets);

        /*
         * Calculate average resolution time using timestamps.
         *
         * Older tickets that existed before resolvedAt was introduced
         * are excluded because their historical resolution timestamp
         * is unknown.
         */
        List<Ticket> ticketsWithResolutionTime =
                tickets.stream()
                        .filter(ticket ->
                                ticket.getCreatedAt() != null)
                        .filter(ticket ->
                                ticket.getResolvedAt() != null)
                        .toList();

        if (ticketsWithResolutionTime.isEmpty()) {

            response.setAverageResolutionTimeHours(
                    null);

        } else {

            double totalResolutionMilliseconds =
                    ticketsWithResolutionTime.stream()
                            .mapToDouble(ticket ->
                                    Duration.between(
                                            ticket.getCreatedAt(),
                                            ticket.getResolvedAt())
                                            .toMillis())
                            .sum();

            double averageResolutionMilliseconds =
                    totalResolutionMilliseconds
                            / ticketsWithResolutionTime.size();

            double averageResolutionTimeHours =
                    averageResolutionMilliseconds
                            / (1000.0 * 60.0 * 60.0);

            response.setAverageResolutionTimeHours(
                    roundToTwoDecimalPlaces(
                            averageResolutionTimeHours));
        }

        return response;
    }

    /**
     * Rounds a numeric analytics value to two decimal places.
     *
     * @param value value to round
     * @return value rounded to two decimal places
     */
    private double roundToTwoDecimalPlaces(
            double value) {

        return Math.round(value * 100.0) / 100.0;
    }
}