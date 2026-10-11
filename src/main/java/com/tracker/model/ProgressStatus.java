package com.tracker.model;

public enum ProgressStatus {
    PENDING("Pending"), IN_PROGRESS("In Progress"), COMPLETED("Completed");

    private final String label;

    ProgressStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}