package com.tracker.dto;

public class StudentStats {

    private final int total;
    private final int pending;
    private final int inProgress;
    private final int completed;
    private final int overdue;

    public StudentStats(int total, int pending, int inProgress, int completed, int overdue) {
        this.total = total;
        this.pending = pending;
        this.inProgress = inProgress;
        this.completed = completed;
        this.overdue = overdue;
    }

    public int getCompletionRate() {
        return total == 0 ? 0 : (int) Math.round(completed * 100.0 / total);
    }

    public int getTotal() { return total; }
    public int getPending() { return pending; }
    public int getInProgress() { return inProgress; }
    public int getCompleted() { return completed; }
    public int getOverdue() { return overdue; }
}