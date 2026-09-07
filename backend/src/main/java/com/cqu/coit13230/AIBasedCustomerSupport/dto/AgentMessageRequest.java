package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object used when a support agent sends
 * a response to a customer support ticket.
 */
public class AgentMessageRequest {

    /**
     * Text content of the support agent's response.
     */
    @NotBlank(message = "Message content is required")
    @Size(
            max = 5000,
            message = "Message content must not exceed 5000 characters"
    )
    private String content;

    /**
     * Returns the support agent's response content.
     *
     * @return the response content
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the support agent's response content.
     *
     * @param content the response content to set
     */
    public void setContent(String content) {
        this.content = content;
    }
}