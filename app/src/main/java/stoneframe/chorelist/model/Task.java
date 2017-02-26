package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public class Task implements Comparable<Task> {

    private DateTime date;

    private String description;
    private int priority;
    private int effort;

    public Task(DateTime date, String description, int priority, int effort) {
        this.date = date;
        this.description = description;
        this.priority = priority;
        this.effort = effort;
    }

    public DateTime getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public int getEffort() {
        return effort;
    }

    public void postpone(int days) {
        date = date.plusDays(days);
    }

    @Override
    public int compareTo(Task other) {
        if (this.equals(other)) {
            return 0;
        }

        int i;
        if ((i = this.date.compareTo(other.date)) != 0) return i;
        if ((i = Integer.compare(this.priority, other.priority)) != 0) return i;
        if ((i = Integer.compare(this.effort, other.effort)) != 0) return i;

        return this.description.compareTo(other.description);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Task)) {
            return false;
        }

        Task other = (Task) obj;

        return this.description.equals(other.description);
    }

    @Override
    public String toString() {
        return description;
    }

}