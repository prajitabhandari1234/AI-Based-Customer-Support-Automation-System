package com.cqu.coit13230.AIBasedCustomerSupport.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
        @NotBlank @Size(max = 10000) String message,
        Long ticketId
) {}
