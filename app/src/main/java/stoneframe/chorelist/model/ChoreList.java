package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.List;
import java.util.stream.Collectors;

public class ChoreList implements ChangeListener,
    AlarmListener
{
    private final Storage storage;
    private final TimeService timeService;
    private final AlarmService alarmService;
    private final NotifierService notifierService;

    private final EffortTracker effortTracker;
    private final ChoreSelector choreSelector;

    private Container container;

    public ChoreList(
        Storage storage,
        TimeService timeService,
        AlarmService alarmService,
        NotifierService notifierService,
        EffortTracker effortTracker,
        ChoreSelector choreSelector)
    {
        this.storage = storage;
        this.timeService = timeService;
        this.alarmService = alarmService;
        this.notifierService = notifierService;
        this.effortTracker = effortTracker;
        this.choreSelector = choreSelector;
    }

    public void load()
    {
        container = storage.load();

        if (container == null)
        {
            container = new Container();

            container.Version = storage.getCurrentVersion();
            container.RoutineManager = new RoutineManager();
            container.ChoreManager = new ChoreManager(effortTracker, choreSelector);
            container.TaskManager = new TaskManager();
        }

        container.RoutineManager.setChangeListener(this);
        container.TaskManager.clean(timeService.getNow());
    }

    public void save()
    {
        storage.save(container);
    }

    public List<Chore> getAllChores()
    {
        return container.ChoreManager.getAllChores();
    }

    public void addChore(Chore chore)
    {
        container.ChoreManager.addChore(chore);
    }

    public void removeChore(Chore chore)
    {
        container.ChoreManager.removeChore(chore);
    }

    public List<Chore> getTodaysChores()
    {
        return container.ChoreManager.getChores(timeService.getNow());
    }

    public void choreDone(Chore chore)
    {
        container.ChoreManager.complete(chore, timeService.getNow());
    }

    public void choreSkip(Chore chore)
    {
        container.ChoreManager.skip(chore, timeService.getNow());
    }

    public int getRemainingEffort()
    {
        return container.ChoreManager.getEffortTracker().getTodaysEffort(timeService.getNow());
    }

    public EffortTracker getEffortTracker()
    {
        return container.ChoreManager.getEffortTracker();
    }

    public List<Task> getAllTasks(boolean includeCompleted)
    {
        return container.TaskManager.getAllTasks(includeCompleted);
    }

    public void addTask(Task task)
    {
        container.TaskManager.addTask(task);
    }

    public void removeTask(Task task)
    {
        container.TaskManager.removeTask(task);
    }

    public List<Task> getTodaysTasks()
    {
        return container.TaskManager.getTodaysTasks(timeService.getNow());
    }

    public void taskDone(Task task)
    {
        container.TaskManager.complete(task, timeService.getNow());
    }

    public void taskUndone(Task task)
    {
        container.TaskManager.undo(task);
    }

    public List<Routine> getAllRoutines()
    {
        return container.RoutineManager.getAllRoutines();
    }

    public void addRoutine(Routine routine)
    {
        container.RoutineManager.addRoutine(routine);
    }

    public void removeRoutine(Routine routine)
    {
        container.RoutineManager.removeRoutine(routine);
    }

    public DateTime getNextRoutineProcedureTime()
    {
        return container.RoutineManager.getNextProcedureTime(timeService.getNow());
    }

    public List<Procedure> getPendingProcedures()
    {
        return container.RoutineManager.getPendingProcedures(timeService.getNow());
    }

    public List<Procedure> getFirstPendingProcedures()
    {
        return container.RoutineManager.getFirstPendingProcedures(timeService.getNow());
    }

    public void procedureDone(Procedure procedure)
    {
        container.RoutineManager.procedureDone(procedure, timeService.getNow());
    }

    @Override
    public void notifyChanged()
    {
        scheduleNextAlarm();
        sendNotification();
    }

    @Override
    public void notifyAlarm()
    {
        scheduleNextAlarm();
        sendNotification();
    }

    private void sendNotification()
    {
        String notificationText = getPendingProcedures()
            .stream()
            .map(Procedure::toString)
            .collect(Collectors.joining(System.lineSeparator()));

        if (!notificationText.isEmpty())
        {
            notifierService.sendNotification(notificationText, "Routine");
        }
    }

    private void scheduleNextAlarm()
    {
        DateTime nextRoutineProcedureTime = getNextRoutineProcedureTime();

        if (nextRoutineProcedureTime != null)
        {
            alarmService.setAlarm(nextRoutineProcedureTime, this);
        }
        else
        {
            alarmService.cancelAlarm();
        }
    }
}
