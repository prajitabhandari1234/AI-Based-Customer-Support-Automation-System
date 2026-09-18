package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a customer message and optional ticket reference for chat.
 *
 * <p>
 * This Data Transfer Object (DTO) is used to receive and validate
 * customer chat requests submitted to the customer support system.
 * </p>
 *
 * <p>
 * The request contains the customer's message and may also include
 * a ticket identifier when the message belongs to an existing support
 * ticket. The validated request data is transferred to the service layer
 * for further processing.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * Message submitted by the customer through the chat interface.
     *
     * <p>
     * The message is required, must not be blank, and is limited
     * to a maximum of 10,000 characters.
     * </p>
     */
    @NotBlank
    @Size(max = 10000)
    private String message;

    /**
     * Optional identifier of an existing support ticket.
     *
     * <p>
     * When provided, this identifier can be used to associate the
     * customer message with an existing ticket. A {@code null} value
     * indicates that no ticket identifier was supplied in the request.
     * </p>
     */
    private Long ticketId;

}