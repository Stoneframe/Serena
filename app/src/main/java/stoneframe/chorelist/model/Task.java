package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Comparator;

public class Task implements Comparable<Task> {

    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    public static final int YEARLY = 3;

    private DateTime next;

    private String description;
    private int priority;
    private int effort;

    private int periodicity;
    private int frequency;

    public Task(String description, int priority, int effort,
                DateTime next, int periodicity, int frequency) {
        this.description = description;
        this.priority = priority;
        this.effort = effort;

        this.next = next;
        this.periodicity = periodicity;
        this.frequency = frequency;
    }

    public DateTime getNext() {
        return new DateTime(next);
    }

    public void setNext(DateTime next) {
        this.next = next;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getEffort() {
        return effort;
    }

    public void setEffort(int effort) {
        this.effort = effort;
    }

    public int getPeriodicity() {
        return periodicity;
    }

    public void setPeriodicity(int periodicity) {
        this.periodicity = periodicity;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void reschedule(DateTime now) {
        if (next == null) {
            next = now;
        }

        while (!next.isAfter(now)) {
            switch (periodicity) {
                case DAILY:
                    next = next.plusDays(frequency);
                    break;
                case WEEKLY:
                    next = next.plusWeeks(frequency);
                    break;
                case MONTHLY:
                    next = next.plusMonths(frequency);
                    break;
                case YEARLY:
                    next = next.plusYears(frequency);
                    break;
                default:
                    return;
            }
        }
    }

    @Override
    public int compareTo(Task other) {
        if (this.equals(other)) {
            return 0;
        }

        int i;
        if ((i = this.next.compareTo(other.next)) != 0) return i;
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

    public static class DutyComparator implements Comparator<Task> {

        private DateTime now;

        public DutyComparator(DateTime now) {
            this.now = now;
        }

        @Override
        public int compare(Task o1, Task o2) {
            int i;

            if (o1.next.isAfter(now) || o2.next.isAfter(now)) {
                if ((i = o1.next.compareTo(o2.next)) != 0) return i;
            } else {
                if ((i = Integer.compare(o1.priority, o2.priority)) != 0) return i;
                if ((i = Integer.compare(o1.effort, o2.effort)) != 0) return i;
            }

            return o1.description.compareTo(o2.description);
        }
    }

}
