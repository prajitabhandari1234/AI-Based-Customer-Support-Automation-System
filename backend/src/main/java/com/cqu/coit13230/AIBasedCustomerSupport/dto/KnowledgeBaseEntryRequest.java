package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries values used to create or update a knowledge base entry.
 *
 * <p>
 * This Data Transfer Object (DTO) contains the information submitted
 * when creating a new knowledge base entry or updating an existing
 * entry within the customer support system.
 * </p>
 *
 * <p>
 * The request contains the question pattern, answer template, category,
 * and active status of the knowledge base entry. The validated request
 * data is transferred to the service layer for further processing.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseEntryRequest {

    /**
     * Question pattern associated with the knowledge base entry.
     *
     * <p>
     * This value is required and must not be blank. It represents
     * the question or query pattern associated with the stored
     * knowledge base information.
     * </p>
     */
    @NotBlank
    private String questionPattern;

    /**
     * Answer template associated with the knowledge base entry.
     *
     * <p>
     * This value is required and must not be blank. It contains
     * the response template associated with the question pattern.
     * </p>
     */
    @NotBlank
    private String answerTemplate;

    /**
     * Category assigned to the knowledge base entry.
     *
     * <p>
     * This value is required and must not be blank. It identifies
     * the category under which the knowledge base entry is organised.
     * </p>
     */
    @NotBlank
    private String category;

    /**
     * Indicates whether the knowledge base entry is active.
     *
     * <p>
     * This value represents the active status of the knowledge
     * base entry.
     * </p>
     */
    private Boolean active;

}