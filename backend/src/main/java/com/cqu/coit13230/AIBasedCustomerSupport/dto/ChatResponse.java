package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the AI chat reply and related ticket details.
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    private String reply;
    private TicketSummaryResponse ticket;
    private AiAnalysisResponse analysis;
}
