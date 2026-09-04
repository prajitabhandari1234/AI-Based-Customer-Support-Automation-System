package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.util.Map;

/**
 * Response DTO containing summary statistics for support tickets.
 *
 * <p>
 * This DTO is used by the administrator analytics API to provide
 * aggregated ticket information such as totals, status distribution,
 * priority distribution, category distribution, and escalation data.
 * </p>
 */
public class AnalyticsSummaryResponse {

    /**
     * Total number of support tickets.
     */
    private long totalTickets;

    /**
     * Number of tickets grouped by lifecycle status.
     */
    private Map<String, Long> byStatus;

    /**
     * Number of tickets grouped by priority level.
     */
    private Map<String, Long> byPriority;

    /**
     * Number of tickets grouped by ticket category.
     */
    private Map<String, Long> byCategory;

    /**
     * Number of tickets currently in escalated status.
     */
    private long escalatedTickets;

    /**
     * Number of tickets currently in resolved status.
     */
    private long resolvedTickets;

    /**
     * Returns the total number of support tickets.
     *
     * @return total ticket count
     */
    public long getTotalTickets() {
        return totalTickets;
    }

    /**
     * Sets the total number of support tickets.
     *
     * @param totalTickets total ticket count
     */
    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    /**
     * Returns ticket counts grouped by status.
     *
     * @return status count map
     */
    public Map<String, Long> getByStatus() {
        return byStatus;
    }

    /**
     * Sets ticket counts grouped by status.
     *
     * @param byStatus status count map
     */
    public void setByStatus(Map<String, Long> byStatus) {
        this.byStatus = byStatus;
    }

    /**
     * Returns ticket counts grouped by priority.
     *
     * @return priority count map
     */
    public Map<String, Long> getByPriority() {
        return byPriority;
    }

    /**
     * Sets ticket counts grouped by priority.
     *
     * @param byPriority priority count map
     */
    public void setByPriority(Map<String, Long> byPriority) {
        this.byPriority = byPriority;
    }

    /**
     * Returns ticket counts grouped by category.
     *
     * @return category count map
     */
    public Map<String, Long> getByCategory() {
        return byCategory;
    }

    /**
     * Sets ticket counts grouped by category.
     *
     * @param byCategory category count map
     */
    public void setByCategory(Map<String, Long> byCategory) {
        this.byCategory = byCategory;
    }

    /**
     * Returns the number of escalated tickets.
     *
     * @return escalated ticket count
     */
    public long getEscalatedTickets() {
        return escalatedTickets;
    }

    /**
     * Sets the number of escalated tickets.
     *
     * @param escalatedTickets escalated ticket count
     */
    public void setEscalatedTickets(long escalatedTickets) {
        this.escalatedTickets = escalatedTickets;
    }

    /**
     * Returns the number of resolved tickets.
     *
     * @return resolved ticket count
     */
    public long getResolvedTickets() {
        return resolvedTickets;
    }

    /**
     * Sets the number of resolved tickets.
     *
     * @param resolvedTickets resolved ticket count
     */
    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }
}