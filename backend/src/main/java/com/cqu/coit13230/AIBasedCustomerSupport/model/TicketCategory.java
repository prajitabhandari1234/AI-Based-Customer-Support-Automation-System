package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Represents the support categories that can be assigned to customer
 * support tickets within the AI-based customer support system.
 *
 * <p>
 * Ticket categories help classify customer enquiries according to
 * the type of assistance required. These categories can be used for
 * ticket organisation, routing, analysis, and support management.
 * </p>
 */
public enum TicketCategory {

    /**
     * Indicates that the ticket relates to billing or payment matters.
     */
    BILLING,

    /**
     * Indicates that the ticket relates to a technical issue or problem.
     */
    TECHNICAL,

    /**
     * Indicates that the ticket relates to a customer's account.
     */
    ACCOUNT,

    /**
     * Indicates that the ticket contains a general customer enquiry.
     */
    GENERAL_INQUIRY,

    /**
     * Indicates that the ticket relates to a refund request or issue.
     */
    REFUND,

    /**
     * Indicates that the ticket relates to the status of a customer order.
     */
    ORDER_STATUS,

    /**
     * Indicates that the ticket relates to product information or
     * product-related questions.
     */
    PRODUCT_INFORMATION

}