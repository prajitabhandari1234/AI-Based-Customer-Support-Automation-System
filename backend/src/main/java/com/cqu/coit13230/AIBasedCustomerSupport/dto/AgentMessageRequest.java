package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a support agent reply sent to a ticket.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * the message content submitted by a support agent when responding
 * to a customer support ticket.
 * </p>
 *
 * <p>
 * The validated message content is transferred to the service layer,
 * where the support agent's reply is processed and associated with
 * the relevant ticket.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentMessageRequest {

    /**
     * Content of the support agent's reply.
     *
     * <p>
     * The message content is required and must not be blank.
     * It is limited to a maximum of 10,000 characters.
     * </p>
     */
    @NotBlank
    @Size(max = 10000)
    private String content;

}