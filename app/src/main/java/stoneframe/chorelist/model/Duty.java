package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public class Duty implements Comparable<Duty> {

    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTLY = 2;
    public static final int YEARLY = 3;

    private DateTime next;

    private String description;
    private int priority;
    private int effort;

    private int periodicity;
    private int frequency;
    private int day;

    public Duty(String description, int priority, int effort,
                DateTime next, int periodicity, int frequency) {
        this.description = description;
        this.priority = priority;
        this.effort = effort;

        this.next = next;
        this.periodicity = periodicity;
        this.frequency = frequency;
        this.day = 1;
    }

    public Duty(String description, int priority, int effort,
                DateTime next, int periodicity, int frequency, int day) {
        this(description, priority, effort, next, periodicity, frequency);
        this.day = day;
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

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
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
                    next = next.plusWeeks(frequency)
                            .plusDays(day - next.getDayOfWeek());
                    break;
                case MONTLY:
                    next = next.plusMonths(frequency)
                            .plusDays(day - next.getDayOfMonth());
                    break;
                case YEARLY:
                    next = next.plusYears(frequency)
                            .plusDays(day - next.getDayOfYear());
                    break;
                default:
                    return;
            }
        }
    }

    public Task createTask() {
        return new Task(new DateTime(next), new String(description), priority, effort);
    }

    @Override
    public int compareTo(Duty other) {
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
        if (!(obj instanceof Duty)) {
            return false;
        }

        Duty other = (Duty) obj;

        return this.description.equals(other.description);
    }

    @Override
    public String toString() {
        return description;
    }
}
