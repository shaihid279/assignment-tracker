package com.tracker.model;

public enum ApprovalStatus {
    PENDING("Pending approval"), APPROVED("Approved"), REJECTED("Rejected");

    private final String label;

    ApprovalStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}