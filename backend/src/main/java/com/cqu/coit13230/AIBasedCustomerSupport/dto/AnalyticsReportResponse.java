package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Returns analytics values for a selected reporting period.
 * The DTO keeps API response data separate from the database entities.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsReportResponse {

    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime generatedAt;
    private AnalyticsSummaryResponse summary;
}
