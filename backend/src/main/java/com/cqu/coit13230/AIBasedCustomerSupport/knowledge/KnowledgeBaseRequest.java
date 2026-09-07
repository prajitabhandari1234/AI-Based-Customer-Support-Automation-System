package com.cqu.coit13230.AIBasedCustomerSupport.knowledge;

import com.cqu.coit13230.AIBasedCustomerSupport.ticket.TicketCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KnowledgeBaseRequest(
        @NotBlank String questionPattern,
        @NotBlank String answerTemplate,
        @NotNull TicketCategory category,
        Boolean active
) {}
