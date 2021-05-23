package com.github.amkaras.tweetus.common.analysis;

public class AnalysisResults {

    private final double consistentCount;
    private final double totalCount;

    public AnalysisResults(double consistentCount, double totalCount) {
        this.consistentCount = consistentCount;
        this.totalCount = totalCount;
    }

    public double getConsistentCount() {
        return consistentCount;
    }

    public double getTotalCount() {
        return totalCount;
    }

    public double getAccuracy() {
        return (consistentCount / totalCount) * 100.0;
    }
}
