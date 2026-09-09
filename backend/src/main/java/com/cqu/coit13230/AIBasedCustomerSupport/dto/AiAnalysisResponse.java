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
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {

    private TicketCategory category;
    private TicketPriority priority;
    private Sentiment sentiment;
    private double sentimentScore;
    private double confidence;
    private boolean escalated;
    private List<String> escalationReasons;
    private boolean knowledgeBaseMatch;
}
