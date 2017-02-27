package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Schedule {

    private EffortTracker effortTracker;
    private TaskSelector taskSelector;

    private List<Task> tasks = new LinkedList<>();

    public Schedule(EffortTracker effortTracker, TaskSelector taskSelector) {
        this.effortTracker = effortTracker;
        this.taskSelector = taskSelector;
    }

    public EffortTracker getEffortTracker() {
        return effortTracker;
    }

    public void addTask(Task task) {
        tasks.add(task);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
    }

    public List<Task> getAllTasks() {
        Collections.sort(tasks, new Comparator<Task>() {
            @Override
            public int compare(Task o1, Task o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        return Collections.unmodifiableList(tasks);
    }

    public List<Task> getTasks() {
        return getTasks(DateTime.now());
    }

    public List<Task> getTasks(DateTime now) {
        Collections.sort(tasks, new Task.DutyComparator(now));

        int effort = effortTracker.getTodaysEffort(now);

        List<Task> list = new LinkedList<>();

        for (Task task : tasks) {
            if (task.getNext().isAfter(now)) {
                break;
            } else {
                list.add(task);
            }
        }

        list = taskSelector.selectTasks(tasks, effort);

        return Collections.unmodifiableList(list);
    }

    public void complete(Task task) {
        complete(task, DateTime.now());
    }

    public void complete(Task task, DateTime now) {
        task.reschedule(now);
        effortTracker.spend(task.getEffort());
    }

    public void skip(Task task) {
        skip(task, DateTime.now());
    }

    public void skip(Task task, DateTime now) {
        task.reschedule(now);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Schedule)) {
            return false;
        }

        Schedule other = (Schedule) obj;

        return this.tasks.equals(other.tasks);
    }

}
