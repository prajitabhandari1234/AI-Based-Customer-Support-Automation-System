package com.cqu.coit13230.AIBasedCustomerSupport.message;

import com.cqu.coit13230.AIBasedCustomerSupport.ai.Sentiment;

import java.time.Instant;

public record MessageView(
        Long id,
        SenderType senderType,
        Long senderId,
        String senderName,
        String content,
        Sentiment sentiment,
        double sentimentScore,
        Instant createdAt
) {
    public static MessageView from(Message message) {
        return new MessageView(
                message.getId(),
                message.getSenderType(),
                message.getSender() == null ? null : message.getSender().getId(),
                message.getSender() == null ? message.getSenderType().name() : message.getSender().getName(),
                message.getContent(),
                message.getSentiment(),
                message.getSentimentScore(),
                message.getCreatedAt()
        );
    }
}
