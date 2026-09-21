package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.util.List;

import com.cqu.coit13230.AIBasedCustomerSupport.model.Sentiment;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketCategory;
import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketPriority;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the AI analysis values for a ticket response.
 *
 * <p>
 * This Data Transfer Object (DTO) contains the results produced by
 * the AI analysis process, including ticket classification, priority,
 * sentiment, confidence, escalation information, and knowledge base
 * matching information.
 * </p>
 *
 * <p>
 * The DTO keeps AI-related API response data separate from the
 * database entities used by the application.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {

    /**
     * Category assigned to the ticket by the AI analysis.
     */
    private TicketCategory category;

    /**
     * Priority level assigned to the ticket by the AI analysis.
     */
    private TicketPriority priority;

    /**
     * Sentiment classification identified from the customer message.
     */
    private Sentiment sentiment;

    /**
     * Numerical score representing the detected sentiment.
     */
    private double sentimentScore;

    /**
     * Confidence value associated with the AI analysis result.
     */
    private double confidence;

    /**
     * Indicates whether the ticket has been escalated.
     */
    private boolean escalated;

    /**
     * List of reasons explaining why the ticket was escalated.
     */
    private List<String> escalationReasons;

    /**
     * Indicates whether relevant information was matched in the
     * knowledge base during processing.
     */
    private boolean knowledgeBaseMatch;

}