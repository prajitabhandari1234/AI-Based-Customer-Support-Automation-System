package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketPriority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class EscalationService {
    public EscalationDecision decide(String message, SentimentResult sentiment,
                                     TicketPriority priority, double confidence) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        List<String> reasons = new ArrayList<>();
        if (Set.of("human", "person", "agent", "manager", "supervisor").stream().anyMatch(lower::contains)) {
            reasons.add("Customer requested a human agent");
        }
        if (confidence < 0.62) reasons.add("AI confidence is below the safe response threshold");
        if (sentiment.score() <= -0.4) reasons.add("Strong negative customer sentiment was detected");
        if (priority == TicketPriority.HIGH || priority == TicketPriority.CRITICAL) {
            reasons.add("Ticket priority requires human review");
        }
        if (Set.of("legal", "privacy", "security breach", "fraud", "medical").stream().anyMatch(lower::contains)) {
            reasons.add("Sensitive or complex issue detected");
        }
        return new EscalationDecision(!reasons.isEmpty(), List.copyOf(reasons));
    }
}
