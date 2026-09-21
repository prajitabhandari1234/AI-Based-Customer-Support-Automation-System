package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns the AI chat reply and related ticket details.
 *
 * <p>
 * This Data Transfer Object (DTO) represents the response returned
 * after a customer chat message has been processed by the support
 * system.
 * </p>
 *
 * <p>
 * The response contains the generated AI reply, summary information
 * about the related support ticket, and the AI analysis results.
 * The DTO keeps API response data separate from the database entities.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * Reply generated for the customer's chat message.
     *
     * <p>
     * This field contains the response that can be returned to the
     * customer through the chat interface.
     * </p>
     */
    private String reply;

    /**
     * Summary information about the support ticket associated
     * with the chat interaction.
     */
    private TicketSummaryResponse ticket;

    /**
     * AI analysis information associated with the chat response.
     *
     * <p>
     * This field contains the analysis results represented by
     * {@link AiAnalysisResponse}.
     * </p>
     */
    private AiAnalysisResponse analysis;

}