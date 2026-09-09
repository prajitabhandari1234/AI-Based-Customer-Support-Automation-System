package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries values used to create or update a knowledge entry.
 * The DTO is used to validate and transfer request data into the service layer.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseEntryRequest {

    @NotBlank
    private String questionPattern;

    @NotBlank
    private String answerTemplate;

    @NotBlank
    private String category;

    private Boolean active;
}
