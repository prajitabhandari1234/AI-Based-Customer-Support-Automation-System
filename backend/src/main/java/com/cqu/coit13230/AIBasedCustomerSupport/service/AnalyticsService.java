package com.cqu.coit13230.AIBasedCustomerSupport.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsReportResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.dto.AnalyticsSummaryResponse;
import com.cqu.coit13230.AIBasedCustomerSupport.model.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;
import com.cqu.coit13230.AIBasedCustomerSupport.repository.TicketRepository;

/**
 * Provides analytics calculations and reporting operations for
 * customer-support tickets.
 *
 * <p>
 * This service builds ticket summaries and analytics reports from
 * persisted ticket records. The calculated information is returned
 * through analytics DTOs for use by the application's dashboard
 * and reporting functionality.
 * </p>
 */
@Service
public class AnalyticsService {

        /**
         * Repository used to retrieve ticket records required for
         * analytics calculations.
         */
        private final TicketRepository ticketRepository;

        /**
         * Creates an analytics service with the repository required to
         * access ticket information.
         *
         * @param ticketRepository repository used to retrieve ticket records
         */
        public AnalyticsService(TicketRepository ticketRepository) {

                this.ticketRepository = ticketRepository;

        }

        /**
         * Generates an analytics summary using all tickets currently
         * available in the repository.
         *
         * @return analytics summary containing calculated ticket metrics
         */
        public AnalyticsSummaryResponse getTicketSummary() {

                return buildSummary(ticketRepository.findAll());

        }

        /**
         * Generates an analytics summary after applying the supplied
         * ticket filters.
         *
         * <p>
         * Tickets can be filtered by creation date range, category,
         * priority, status, and minimum or maximum sentiment score.
         * A {@code null} filter value means that the corresponding
         * criterion is not applied.
         * </p>
         *
         * @param startDate    earliest ticket creation date to include
         * @param endDate      latest ticket creation date to include
         * @param category     ticket category to include
         * @param priority     ticket priority to include
         * @param status       ticket status to include
         * @param minSentiment minimum sentiment score to include
         * @param maxSentiment maximum sentiment score to include
         * @return analytics summary calculated from the filtered tickets
         */
        public AnalyticsSummaryResponse getFilteredTicketSummary(

                        LocalDate startDate,

                        LocalDate endDate,

                        TicketCategory category,

                        TicketPriority priority,

                        TicketStatus status,

                        Double minSentiment,

                        Double maxSentiment) {

                List<Ticket> filtered = ticketRepository.findAll().stream()

                                .filter(ticket -> startDate == null

                                                || !ticket.getCreatedAt().toLocalDate().isBefore(startDate))

                                .filter(ticket -> endDate == null

                                                || !ticket.getCreatedAt().toLocalDate().isAfter(endDate))

                                .filter(ticket -> category == null || ticket.getCategory() == category)

                                .filter(ticket -> priority == null || ticket.getPriority() == priority)

                                .filter(ticket -> status == null || ticket.getStatus() == status)

                                .filter(ticket -> minSentiment == null

                                                || value(ticket.getSentimentScore()) >= minSentiment)

                                .filter(ticket -> maxSentiment == null

                                                || value(ticket.getSentimentScore()) <= maxSentiment)

                                .toList();

                return buildSummary(filtered);

        }

        /**
         * Generates a weekly analytics report for the week containing
         * the supplied date.
         *
         * <p>
         * The reporting period begins on Monday and ends six days later.
         * </p>
         *
         * @param date date used to determine the reporting week
         * @return weekly analytics report
         */
        public AnalyticsReportResponse getWeeklyReport(LocalDate date) {

                LocalDate startDate = date.with(java.time.DayOfWeek.MONDAY);

                LocalDate endDate = startDate.plusDays(6);

                return report("WEEKLY", startDate, endDate);

        }

        /**
         * Generates a monthly analytics report for the month containing
         * the supplied date.
         *
         * @param date date used to determine the reporting month
         * @return monthly analytics report
         */
        public AnalyticsReportResponse getMonthlyReport(LocalDate date) {

                LocalDate startDate = date.with(TemporalAdjusters.firstDayOfMonth());

                LocalDate endDate = date.with(TemporalAdjusters.lastDayOfMonth());

                return report("MONTHLY", startDate, endDate);

        }

        /**
         * Creates an analytics report for the specified reporting period.
         *
         * <p>
         * Tickets created between the beginning of the start date and the
         * end of the final date are retrieved and used to build the
         * analytics summary included in the report.
         * </p>
         *
         * @param reportType type of report being generated
         * @param startDate  first date included in the reporting period
         * @param endDate    final date included in the reporting period
         * @return analytics report for the specified period
         */
        private AnalyticsReportResponse report(

                        String reportType,

                        LocalDate startDate,

                        LocalDate endDate) {

                LocalDateTime start = startDate.atStartOfDay();

                LocalDateTime end = endDate.plusDays(1).atStartOfDay().minusNanos(1);

                List<Ticket> tickets = ticketRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

                return new AnalyticsReportResponse(

                                reportType,

                                startDate,

                                endDate,

                                LocalDateTime.now(),

                                buildSummary(tickets));

        }

        /**
         * Calculates the main analytics and dashboard values from the
         * supplied collection of tickets.
         *
         * <p>
         * The calculated information includes total, open, resolved, and
         * escalated ticket counts, escalation and chatbot success rates,
         * average response and resolution times, grouped ticket statistics,
         * sentiment distribution, and daily ticket volume.
         * </p>
         *
         * @param tickets tickets used to calculate the analytics metrics
         * @return analytics summary containing the calculated values
         */
        private AnalyticsSummaryResponse buildSummary(List<Ticket> tickets) {

                long total = tickets.size();

                long escalated = tickets.stream().filter(Ticket::isEscalated).count();

                long resolved = tickets.stream()

                                .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED

                                                || ticket.getStatus() == TicketStatus.RESOLVED_BY_AI

                                                || ticket.getStatus() == TicketStatus.CLOSED)

                                .count();

                long open = total - resolved;

                long resolvedByAi = tickets.stream()

                                .filter(ticket -> ticket.getStatus() == TicketStatus.RESOLVED_BY_AI)

                                .count();

                double averageFirstResponseSeconds = tickets.stream()

                                .filter(ticket -> ticket.getFirstResponseAt() != null)

                                .mapToLong(ticket -> Duration.between(

                                                ticket.getCreatedAt(),

                                                ticket.getFirstResponseAt()).toSeconds())

                                .average()

                                .orElse(0);

                double averageResolutionMinutes = tickets.stream()

                                .filter(ticket -> ticket.getResolvedAt() != null)

                                .mapToLong(ticket -> Duration.between(

                                                ticket.getCreatedAt(),

                                                ticket.getResolvedAt()).toMinutes())

                                .average()

                                .orElse(0);

                AnalyticsSummaryResponse response = new AnalyticsSummaryResponse();

                response.setTotalTickets(total);

                response.setEnquiryCount(total);

                response.setOpenTickets(open);

                response.setResolvedTickets(resolved);

                response.setEscalatedTickets(escalated);

                response.setEscalationRatePercent(percent(escalated, total));

                response.setEscalationRate(percent(escalated, total));

                response.setChatbotSuccessRate(percent(resolvedByAi, total));

                response.setAverageFirstResponseSeconds(round(averageFirstResponseSeconds));

                response.setAverageResolutionMinutes(round(averageResolutionMinutes));

                response.setAverageResponseTimeHours(roundNullable(averageFirstResponseSeconds / 3600.0, total));

                response.setAverageResolutionTimeHours(roundNullable(averageResolutionMinutes / 60.0, total));

                response.setByStatus(group(tickets, ticket -> ticket.getStatus().name()));

                response.setByPriority(group(tickets, ticket -> ticket.getPriority().name()));

                response.setByCategory(group(tickets, ticket -> ticket.getCategory().name()));

                response.setSentimentDistribution(group(

                                tickets,

                                ticket -> ticket.getSentiment() == null ? "NEUTRAL" : ticket.getSentiment().name()));

                response.setDailyTicketVolume(dailyVolume(tickets, 7));

                return response;

        }

        /**
         * Groups tickets according to the supplied classifier and counts
         * the number of tickets belonging to each generated group.
         *
         * @param tickets    tickets to group
         * @param classifier function used to determine the group key for each ticket
         * @return map containing each group name and its corresponding ticket count
         */
        private Map<String, Long> group(

                        List<Ticket> tickets,

                        Function<Ticket, String> classifier) {

                return tickets.stream().collect(Collectors.groupingBy(

                                classifier,

                                LinkedHashMap::new,

                                Collectors.counting()));

        }

        /**
         * Calculates ticket creation volume for a specified number of
         * recent calendar days.
         *
         * <p>
         * Each date is stored as a map key with the number of tickets
         * created on that date as its value. This data can be used by
         * the frontend to display a daily ticket trend.
         * </p>
         *
         * @param tickets tickets used to calculate daily volume
         * @param days    number of recent days to include
         * @return map containing each date and its ticket creation count
         */
        private Map<String, Long> dailyVolume(List<Ticket> tickets, int days) {

                Map<String, Long> result = new LinkedHashMap<>();

                LocalDate today = LocalDate.now();

                for (int offset = days - 1; offset >= 0; offset--) {

                        LocalDate day = today.minusDays(offset);

                        long count = tickets.stream()

                                        .filter(ticket -> ticket.getCreatedAt().toLocalDate().equals(day))

                                        .count();

                        result.put(day.toString(), count);

                }

                return result;

        }

        /**
         * Converts a nullable {@link Double} value into a primitive value.
         *
         * @param value value to convert
         * @return {@code 0} when the supplied value is {@code null};
         *         otherwise the original numeric value
         */
        private double value(Double value) {

                return value == null ? 0 : value;

        }

        /**
         * Calculates a percentage using the supplied numerator and denominator.
         *
         * @param numerator   value representing the selected portion
         * @param denominator value representing the total
         * @return calculated percentage rounded to two decimal places,
         *         or {@code 0} when the denominator is zero
         */
        private double percent(long numerator, long denominator) {

                return denominator == 0 ? 0 : round(numerator * 100.0 / denominator);

        }

        /**
         * Rounds a value when ticket data exists and returns {@code null}
         * when the total ticket count is zero.
         *
         * @param value numeric value to round
         * @param total total number of tickets
         * @return rounded value when tickets exist, or {@code null} when
         *         the total is zero
         */
        private Double roundNullable(double value, long total) {

                return total == 0 ? null : round(value);

        }

        /**
         * Rounds a numeric value to two decimal places.
         *
         * @param value value to round
         * @return value rounded to two decimal places
         */
        private double round(double value) {

                return Math.round(value * 100.0) / 100.0;

        }

}