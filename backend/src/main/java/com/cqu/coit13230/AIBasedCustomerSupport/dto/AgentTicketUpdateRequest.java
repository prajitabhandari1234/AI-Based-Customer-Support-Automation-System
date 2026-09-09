package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import com.cqu.coit13230.AIBasedCustomerSupport.model.TicketStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries ticket updates submitted by a support agent.
 * The DTO is used to validate and transfer request data into the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentTicketUpdateRequest {

    @NotNull
    private TicketStatus status;
    private String resolutionNotes;
}
