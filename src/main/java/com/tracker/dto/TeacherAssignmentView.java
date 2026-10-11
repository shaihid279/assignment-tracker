package com.tracker.dto;

import com.tracker.model.Assignment;

/** A teacher's assignment together with class-wide completion numbers. */
public class TeacherAssignmentView {

    private final Assignment assignment;
    private final long totalStudents;
    private final long completed;
    private final long inProgress;
    private final boolean pastDue;

    public TeacherAssignmentView(Assignment assignment, long totalStudents, long completed,
                                 long inProgress, boolean pastDue) {
        this.assignment = assignment;
        this.totalStudents = totalStudents;
        this.completed = completed;
        this.inProgress = inProgress;
        this.pastDue = pastDue;
    }

    public int getCompletionPercent() {
        return totalStudents == 0 ? 0 : (int) Math.round(completed * 100.0 / totalStudents);
    }

    public long getPending() {
        return Math.max(0, totalStudents - completed - inProgress);
    }

    public Assignment getAssignment() { return assignment; }
    public long getTotalStudents() { return totalStudents; }
    public long getCompleted() { return completed; }
    public long getInProgress() { return inProgress; }
    public boolean isPastDue() { return pastDue; }
}