package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;

@Service
public class SentimentService {
    private static final Set<String> NEGATIVE = Set.of(
            "angry", "annoyed", "bad", "broken", "complaint", "disappointed", "failure", "frustrated",
            "furious", "hate", "horrible", "late", "never", "poor", "ridiculous", "scam", "terrible",
            "unacceptable", "unhappy", "useless", "worst", "wrong", "refund", "urgent"
    );
    private static final Set<String> POSITIVE = Set.of(
            "amazing", "awesome", "excellent", "good", "great", "happy", "helpful", "love",
            "perfect", "pleased", "satisfied", "thanks", "thank", "wonderful"
    );

    public SentimentResult analyse(String text) {
        if (text == null || text.isBlank()) return new SentimentResult(Sentiment.NEUTRAL, 0);
        String[] words = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        double score = 0;
        for (String word : words) {
            if (NEGATIVE.contains(word)) score -= 0.22;
            if (POSITIVE.contains(word)) score += 0.20;
        }
        if (text.contains("!!!")) score -= 0.18;
        if (text.equals(text.toUpperCase(Locale.ROOT)) && text.length() > 8) score -= 0.15;
        score = Math.max(-1, Math.min(1, score));
        Sentiment sentiment = score <= -0.2 ? Sentiment.NEGATIVE : score >= 0.2 ? Sentiment.POSITIVE : Sentiment.NEUTRAL;
        return new SentimentResult(sentiment, round(score));
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
