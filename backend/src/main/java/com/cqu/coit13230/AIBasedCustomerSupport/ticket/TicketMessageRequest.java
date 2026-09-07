package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TicketMessageRequest(
        @NotBlank @Size(max = 10000) String message
) {}
