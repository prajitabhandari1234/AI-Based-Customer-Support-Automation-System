package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Carries a new message submitted to an existing ticket.
 *
 * <p>
 * This Data Transfer Object (DTO) contains message information
 * submitted when adding a new message to an existing support ticket.
 * </p>
 *
 * <p>
 * The request supports both {@code message} and {@code content}
 * fields. The {@link #text()} method determines which value should
 * be used when processing the submitted ticket message.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketMessageRequest {

    /**
     * Message submitted to the existing support ticket.
     *
     * <p>
     * When this field contains a non-blank value, it is returned
     * by the {@link #text()} method.
     * </p>
     */
    private String message;

    /**
     * Alternative content field for the submitted ticket message.
     *
     * <p>
     * This value is returned by the {@link #text()} method when
     * the {@code message} field is {@code null} or blank.
     * </p>
     */
    private String content;

    /**
     * Returns the text that should be used for the ticket message.
     *
     * <p>
     * If the {@code message} field is not {@code null} and is not
     * blank, its value is returned. Otherwise, the value stored in
     * the {@code content} field is returned.
     * </p>
     *
     * @return the non-blank message value when available; otherwise,
     *         the content value
     */
    public String text() {

        if (message != null && !message.isBlank()) {

            return message;

        }

        return content;

    }

}