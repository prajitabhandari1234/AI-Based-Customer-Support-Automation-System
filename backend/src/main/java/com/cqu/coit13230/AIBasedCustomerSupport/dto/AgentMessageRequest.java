package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a support agent reply sent to a ticket.
 * The DTO is used to validate and transfer request data into the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessageRequest {

    @NotBlank
    @Size(max = 10000)
    private String content;
}
