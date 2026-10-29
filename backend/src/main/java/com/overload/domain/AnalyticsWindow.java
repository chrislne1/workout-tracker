package com.overload.domain;

public enum AnalyticsWindow {
    D30(30), D90(90), M6(182), Y1(365), AOT(null);

    private final Integer days;
    AnalyticsWindow(Integer days) { this.days = days; }
    public Integer getDays() { return days; }
}
