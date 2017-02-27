package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public class SimpleEffortTracker implements EffortTracker {

    private DateTime previous;

    private int maxEffort;
    private int remainingEffort;

    public SimpleEffortTracker(int effort) {
        maxEffort = effort;
        remainingEffort = maxEffort;
    }

    @Override
    public int getTodaysEffort(DateTime now) {
        if (!isSameDay(previous, now)) {
            previous = now;
            remainingEffort = maxEffort;
        }

        return remainingEffort;
    }

    @Override
    public void spend(int effort) {
        if (effort > remainingEffort) {
            remainingEffort = 0;
        } else {
            remainingEffort -= effort;
        }
    }

    private boolean isSameDay(DateTime d1, DateTime d2) {
        return d1 != null && d2 != null
                && d1.getYear() == d2.getYear()
                && d1.getMonthOfYear() == d2.getMonthOfYear()
                && d1.getDayOfMonth() == d2.getDayOfMonth();
    }

}
