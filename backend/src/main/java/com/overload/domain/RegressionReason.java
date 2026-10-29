package com.overload.domain;

public enum RegressionReason {
    PURPOSEFUL_DELOAD(true), FATIGUE_POOR_SLEEP(false), INJURY_DISCOMFORT(true), EQUIPMENT_CHANGE(false);

    private final boolean excludedFromTrend;
    RegressionReason(boolean excludedFromTrend) { this.excludedFromTrend = excludedFromTrend; }

    /** Sessions with these reasons are removed from the rate-of-progression trendline. */
    public boolean isExcludedFromTrend() { return excludedFromTrend; }
}
