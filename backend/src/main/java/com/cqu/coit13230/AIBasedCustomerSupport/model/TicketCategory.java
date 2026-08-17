package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Defines the categories used to classify customer support tickets.
 *
 * <p>Ticket categories help organize customer issues and support
 * appropriate routing and handling within the system.</p>
 */
public enum TicketCategory {

    /** Represents issues related to billing, payments, or charges. */
    BILLING,

    /** Represents technical issues requiring troubleshooting or assistance. */
    TECHNICAL,

    /** Represents issues related to customer accounts or account access. */
    ACCOUNT,

    /** Represents general enquiries that do not belong to another category. */
    GENERAL_INQUIRY,

    /** Represents requests or issues related to refunds. */
    REFUND
}