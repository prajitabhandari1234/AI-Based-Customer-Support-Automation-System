package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a customer message and optional ticket reference for chat.
 * The DTO is used to validate and transfer request data into the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    @NotBlank
    @Size(max = 10000)
    private String message;

    private Long ticketId;
}
