package com.cqu.coit13230.AIBasedCustomerSupport.ticket;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 10000) String message
) {}
