package com.tracker.dto;

import com.tracker.model.ProgressStatus;
import com.tracker.model.User;

public class RosterRow {

    private final User student;
    private final ProgressStatus status;
    private final int percent;

    public RosterRow(User student, ProgressStatus status, int percent) {
        this.student = student;
        this.status = status;
        this.percent = percent;
    }

    public User getStudent() { return student; }
    public ProgressStatus getStatus() { return status; }
    public int getPercent() { return percent; }
}