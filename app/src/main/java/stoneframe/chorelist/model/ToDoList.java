package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ToDoList {

    private Schedule schedule;

    private TaskSelector taskSelector;
    private EffortTracker effortTracker;

    private List<Task> tasks = new LinkedList<>();

    public ToDoList(Schedule schedule, TaskSelector taskSelector, EffortTracker effortTracker) {
        this.schedule = schedule;
        this.taskSelector = taskSelector;
        this.effortTracker = effortTracker;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public List<Task> getTasks(DateTime now) {
        for (Task task : schedule.getTasks(now)) {
            if (!tasks.contains(task)) {
                tasks.add(task);
            }
        }
        Collections.sort(tasks);

        int effort = effortTracker.getTodaysEffort(now);

        List<Task> list = taskSelector.selectTasks(this.tasks, effort);

        return Collections.unmodifiableList(list);
    }

    public void complete(Task task) {
        tasks.remove(task);
        effortTracker.spend(task.getEffort());
    }

    public void skip(Task task) {
        tasks.remove(task);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ToDoList)) {
            return false;
        }

        ToDoList other = (ToDoList) obj;

        return this.schedule.equals(other.schedule)
                && this.taskSelector.getClass().equals(other.taskSelector.getClass())
                && this.tasks.equals(other.tasks);
    }

}
