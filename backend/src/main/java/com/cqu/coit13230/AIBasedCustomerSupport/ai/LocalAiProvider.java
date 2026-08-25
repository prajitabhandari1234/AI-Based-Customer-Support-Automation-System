package com.cqu.coit13230.AIBasedCustomerSupport.ai;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LocalAiProvider {
    public String generate(String customerMessage, TicketCategory category, Optional<String> knowledgeAnswer,
                           boolean escalated) {
        if (knowledgeAnswer.isPresent()) return knowledgeAnswer.get();
        String base = switch (category) {
            case ACCOUNT -> "I can help with account access. Please confirm whether you need login help, an email update, or password-reset guidance.";
            case BILLING -> "I understand this is about billing. Please provide the invoice or transaction reference without sharing full card details.";
            case REFUND -> "I can help with your refund request. Please provide the order number and a short reason for the return.";
            case ORDER_STATUS -> "Please provide your order number so the delivery or tracking status can be checked.";
            case TECHNICAL -> "I am sorry you are experiencing a technical issue. Please share the error message, device, browser, and the steps that caused the problem.";
            case PRODUCT_INFORMATION -> "Please tell me which product or feature you are asking about, and I will provide the relevant information.";
            case GENERAL_INQUIRY -> "Thank you for contacting support. I have reviewed your message and will help you with the next appropriate step.";
        };
        if (escalated) {
            return base + " I have also escalated this conversation to a human support agent for review.";
        }
        return base;
    }
}
