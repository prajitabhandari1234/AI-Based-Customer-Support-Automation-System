package com.cqu.coit13230.AIBasedCustomerSupport.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data transfer object representing a periodic administrator
 * analytics report.
 *
 * <p>
 * Contains the reporting period, generation timestamp,
 * and aggregated support-ticket statistics.
 * </p>
 */
public class AnalyticsReportResponse {

    private String reportType;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDateTime generatedAt;

    private AnalyticsSummaryResponse summary;

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(
            LocalDateTime generatedAt) {

        this.generatedAt = generatedAt;
    }

    public AnalyticsSummaryResponse getSummary() {
        return summary;
    }

    public void setSummary(
            AnalyticsSummaryResponse summary) {

        this.summary = summary;
    }
}