package com.tracker.dto;

import com.tracker.model.Assignment;
import com.tracker.model.ProgressStatus;

/** A single assignment as seen by a student, combined with that student's progress. */
public class AssignmentView {

    private final Assignment assignment;
    private final ProgressStatus status;
    private final int percent;
    private final boolean overdue;

    public AssignmentView(Assignment assignment, ProgressStatus status, int percent, boolean overdue) {
        this.assignment = assignment;
        this.status = status;
        this.percent = percent;
        this.overdue = overdue;
    }

    public Assignment getAssignment() { return assignment; }
    public ProgressStatus getStatus() { return status; }
    public int getPercent() { return percent; }
    public boolean isOverdue() { return overdue; }
}