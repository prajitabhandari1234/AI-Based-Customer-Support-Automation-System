package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketPriority;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ClassificationService {
    private static final Map<TicketCategory, Set<String>> CATEGORY_WORDS = Map.of(
            TicketCategory.BILLING, Set.of("bill", "billing", "charge", "charged", "invoice", "payment", "price"),
            TicketCategory.TECHNICAL, Set.of("bug", "crash", "error", "failed", "not working", "technical", "broken"),
            TicketCategory.ACCOUNT, Set.of("account", "login", "password", "reset", "locked", "email"),
            TicketCategory.REFUND, Set.of("refund", "return", "money back", "cancel"),
            TicketCategory.ORDER_STATUS, Set.of("order", "delivery", "tracking", "shipment", "arrive"),
            TicketCategory.PRODUCT_INFORMATION, Set.of("product", "feature", "specification", "available", "stock")
    );

    public TicketCategory classify(String message) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        TicketCategory best = TicketCategory.GENERAL_INQUIRY;
        int bestScore = 0;
        for (Map.Entry<TicketCategory, Set<String>> entry : CATEGORY_WORDS.entrySet()) {
            int score = (int) entry.getValue().stream().filter(lower::contains).count();
            if (score > bestScore) {
                best = entry.getKey();
                bestScore = score;
            }
        }
        return best;
    }

    public TicketPriority priority(String message, TicketCategory category, SentimentResult sentiment) {
        String lower = message == null ? "" : message.toLowerCase(Locale.ROOT);
        boolean criticalWord = Set.of("security breach", "data leak", "fraud", "emergency", "critical").stream().anyMatch(lower::contains);
        boolean urgentWord = Set.of("urgent", "asap", "immediately", "today", "now").stream().anyMatch(lower::contains);
        if (criticalWord || sentiment.score() <= -0.8) return TicketPriority.CRITICAL;
        if (urgentWord || sentiment.score() <= -0.4 || category == TicketCategory.REFUND) return TicketPriority.HIGH;
        if (category == TicketCategory.GENERAL_INQUIRY || category == TicketCategory.PRODUCT_INFORMATION) return TicketPriority.LOW;
        return TicketPriority.MEDIUM;
    }
}
