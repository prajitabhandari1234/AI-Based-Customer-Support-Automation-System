package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import ccom.cqu.coit13230.AIBasedCustomerSupport.knowledge.KnowledgeBaseService;
import com.cqu.coit13230.AIBasedCustomerSupport.knowledge.KnowledgeMatch;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketPriority;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AiProcessingService {
    private final SentimentService sentimentService;
    private final ClassificationService classificationService;
    private final EscalationService escalationService;
    private final KnowledgeBaseService knowledgeBase;
    private final LocalAiProvider localAi;
    private final OpenAiProvider openAi;
    private final String provider;

    public AiProcessingService(SentimentService sentimentService,
                               ClassificationService classificationService,
                               EscalationService escalationService,
                               KnowledgeBaseService knowledgeBase,
                               LocalAiProvider localAi,
                               OpenAiProvider openAi,
                               @Value("${app.ai.provider}") String provider) {
        this.sentimentService = sentimentService;
        this.classificationService = classificationService;
        this.escalationService = escalationService;
        this.knowledgeBase = knowledgeBase;
        this.localAi = localAi;
        this.openAi = openAi;
        this.provider = provider;
    }

    @Transactional(readOnly = true)
    public AiResult process(String message) {
        SentimentResult sentiment = sentimentService.analyse(message);
        Optional<KnowledgeMatch> match = knowledgeBase.findBestMatch(message);
        TicketCategory category = match.map(m -> m.entry().getCategory())
                .orElseGet(() -> classificationService.classify(message));
        TicketPriority priority = classificationService.priority(message, category, sentiment);

        double confidence = match.map(m -> Math.max(0.72, Math.min(0.97, m.score())))
                .orElse("openai".equalsIgnoreCase(provider) && openAi.isConfigured() ? 0.78 : 0.58);

        EscalationDecision decision = escalationService.decide(message, sentiment, priority, confidence);
        Optional<String> answer = match.map(m -> m.entry().getAnswerTemplate());

        String reply = Optional.<String>empty()
                .or(() -> "openai".equalsIgnoreCase(provider)
                        ? openAi.generate(message, category, sentiment.sentiment(), decision.escalate())
                        : Optional.empty())
                .orElseGet(() -> localAi.generate(message, category, answer, decision.escalate()));

        return new AiResult(
                reply,
                category,
                priority,
                sentiment.sentiment(),
                sentiment.score(),
                round(confidence),
                decision.escalate(),
                decision.reasons(),
                match.isPresent()
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
