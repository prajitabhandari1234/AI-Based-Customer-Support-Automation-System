package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns analytics values for a selected reporting period.
 *
 * <p>
 * This Data Transfer Object (DTO) represents an analytics report
 * generated for a specific reporting period within the customer
 * support system.
 * </p>
 *
 * <p>
 * The DTO contains information about the report type, reporting
 * period, generation time, and the associated analytics summary.
 * It keeps API response data separate from the database entities.
 * </p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReportResponse {

    /**
     * Type of analytics report being returned.
     *
     * <p>
     * This value identifies the reporting period or report category,
     * such as a weekly or monthly report.
     * </p>
     */
    private String reportType;

    /**
     * Starting date of the reporting period.
     */
    private LocalDate startDate;

    /**
     * Ending date of the reporting period.
     */
    private LocalDate endDate;

    /**
     * Date and time when the analytics report was generated.
     */
    private LocalDateTime generatedAt;

    /**
     * Analytics summary associated with the selected reporting period.
     *
     * <p>
     * This object contains the calculated summary values included
     * in the generated analytics report.
     * </p>
     */
    private AnalyticsSummaryResponse summary;

}