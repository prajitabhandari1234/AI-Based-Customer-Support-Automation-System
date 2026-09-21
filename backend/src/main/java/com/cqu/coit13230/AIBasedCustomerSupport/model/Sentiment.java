package com.cqu.coit13230.AIBasedCustomerSupport.model;

/**
 * Represents the sentiment values used when analysing customer
 * support tickets and messages.
 *
 * <p>
 * Sentiment classification allows the system to identify the general
 * emotional tone of customer interactions. A sentiment can be
 * classified as positive, neutral, or negative based on the
 * analysis performed by the system.
 * </p>
 */
public enum Sentiment {

    /**
     * Indicates that the analysed content has a positive sentiment.
     */
    POSITIVE,

    /**
     * Indicates that the analysed content has a neutral sentiment.
     */
    NEUTRAL,

    /**
     * Indicates that the analysed content has a negative sentiment.
     */
    NEGATIVE

}