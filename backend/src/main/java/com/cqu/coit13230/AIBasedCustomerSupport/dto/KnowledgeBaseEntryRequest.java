package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO used for creating and updating knowledge base entries.
 *
 * <p>
 * The authenticated user responsible for creating or updating the
 * knowledge base entry is not supplied by the client. Instead, the
 * backend determines the user from JWT authentication.
 * </p>
 */
public class KnowledgeBaseEntryRequest {

    /**
     * Question pattern used to identify matching customer enquiries.
     */
    @NotBlank(message = "Question pattern is required")
    private String questionPattern;

    /**
     * Predefined response template associated with the question pattern.
     */
    @NotBlank(message = "Answer template is required")
    private String answerTemplate;

    /**
     * Category used to organise the knowledge base entry.
     */
    @NotBlank(message = "Category is required")
    private String category;

    /**
     * Returns the question pattern.
     *
     * @return question pattern
     */
    public String getQuestionPattern() {
        return questionPattern;
    }

    /**
     * Sets the question pattern.
     *
     * @param questionPattern question pattern
     */
    public void setQuestionPattern(String questionPattern) {
        this.questionPattern = questionPattern;
    }

    /**
     * Returns the answer template.
     *
     * @return answer template
     */
    public String getAnswerTemplate() {
        return answerTemplate;
    }

    /**
     * Sets the answer template.
     *
     * @param answerTemplate answer template
     */
    public void setAnswerTemplate(String answerTemplate) {
        this.answerTemplate = answerTemplate;
    }

    /**
     * Returns the category.
     *
     * @return knowledge base category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     *
     * @param category knowledge base category
     */
    public void setCategory(String category) {
        this.category = category;
    }
}