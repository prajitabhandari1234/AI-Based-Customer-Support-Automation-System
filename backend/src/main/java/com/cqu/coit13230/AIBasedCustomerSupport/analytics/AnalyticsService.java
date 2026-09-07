package com.cqu.coit13230.AIBasedCustomerSupport.analytics;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.Ticket;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketRepository;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    private final TicketRepository tickets;

    public AnalyticsService(TicketRepository tickets) {
        this.tickets = tickets;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummary summary() {
        List<Ticket> all = tickets.findAll();
        long total = all.size();
        long escalated = all.stream().filter(Ticket::isEscalated).count();
        long resolved = all.stream().filter(t -> Set.of(TicketStatus.RESOLVED, TicketStatus.RESOLVED_BY_AI, TicketStatus.CLOSED).contains(t.getStatus())).count();
        long open = total - resolved;

        double avgResponse = all.stream()
                .filter(t -> t.getFirstResponseAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getFirstResponseAt()).toSeconds())
                .average().orElse(0);
        double avgResolution = all.stream()
                .filter(t -> t.getResolvedAt() != null)
                .mapToLong(t -> Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes())
                .average().orElse(0);

        return new AnalyticsSummary(
                total, open, resolved, escalated,
                percent(escalated, total),
                percent(all.stream().filter(t -> t.getStatus() == TicketStatus.RESOLVED_BY_AI).count(), total),
                round(avgResponse),
                round(avgResolution),
                group(all, t -> t.getStatus().name()),
                group(all, t -> t.getCategory().name()),
                group(all, t -> t.getPriority().name()),
                group(all, t -> t.getSentiment().name()),
                dailyVolume(all, 7)
        );
    }

    private Map<String, Long> group(List<Ticket> all, Function<Ticket, String> classifier) {
        return all.stream().collect(Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.counting()));
    }

    private Map<String, Long> dailyVolume(List<Ticket> all, int days) {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now(zone);
        Map<String, Long> result = new LinkedHashMap<>();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd MMM");
        for (int offset = days - 1; offset >= 0; offset--) {
            LocalDate day = today.minusDays(offset);
            long count = all.stream()
                    .filter(ticket -> LocalDate.ofInstant(ticket.getCreatedAt(), zone).equals(day))
                    .count();
            result.put(day.format(format), count);
        }
        return result;
    }

    private double percent(long numerator, long denominator) {
        return denominator == 0 ? 0 : round(numerator * 100.0 / denominator);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
