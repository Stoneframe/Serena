package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Schedule {

    private List<Duty> duties = new LinkedList<>();

    public Schedule() {

    }

    public void addDuty(Duty duty) {
        duties.add(duty);
    }

    public void removeDuty(Duty duty) {
        duties.remove(duty);
    }

    public List<Duty> getDuties() {
        return Collections.unmodifiableList(duties);
    }

    public List<Task> getTasks(DateTime now) {
        Collections.sort(duties);

        List<Task> list = new ArrayList<>();
        for (Duty duty : duties) {
            if (duty.getNext().isAfter(now)) {
                break;
            } else {
                list.add(duty.createTask());
                duty.reschedule(now);
            }
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Schedule)) {
            return false;
        }

        Schedule other = (Schedule) obj;

        return this.duties.equals(other.duties);
    }

}
