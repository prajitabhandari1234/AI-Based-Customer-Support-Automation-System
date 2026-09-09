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
 * Builds ticket summaries and analytics reports.
 * The calculations are built from ticket records and returned in DTOs for the dashboard.
 */
@Service
public class AnalyticsService {

    private final TicketRepository ticketRepository;

    public AnalyticsService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public AnalyticsSummaryResponse getTicketSummary() {
        return buildSummary(ticketRepository.findAll());
    }

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

    public AnalyticsReportResponse getWeeklyReport(LocalDate date) {
        LocalDate startDate = date.with(java.time.DayOfWeek.MONDAY);
        LocalDate endDate = startDate.plusDays(6);
        return report("WEEKLY", startDate, endDate);
    }

    public AnalyticsReportResponse getMonthlyReport(LocalDate date) {
        LocalDate startDate = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endDate = date.with(TemporalAdjusters.lastDayOfMonth());
        return report("MONTHLY", startDate, endDate);
    }

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

    /*
     * Calculates the main dashboard values from the supplied ticket list.
     * This includes totals, status counts, escalation rate and average AI-related values.
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

    private Map<String, Long> group(
            List<Ticket> tickets,
            Function<Ticket, String> classifier) {

        return tickets.stream().collect(Collectors.groupingBy(
                classifier,
                LinkedHashMap::new,
                Collectors.counting()));
    }

    // Groups ticket creation dates so the frontend can display a simple daily trend.
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

    private double value(Double value) {
        return value == null ? 0 : value;
    }

    private double percent(long numerator, long denominator) {
        return denominator == 0 ? 0 : round(numerator * 100.0 / denominator);
    }

    private Double roundNullable(double value, long total) {
        return total == 0 ? null : round(value);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
