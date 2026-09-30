package stoneframe.serena.gui;

import android.app.Application;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;

import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.balancers.Balancer;
import stoneframe.serena.checklists.Checklist;
import stoneframe.serena.chores.Chore;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.notes.NoteGroupView;
import stoneframe.serena.notes.NoteView;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.routines.Routine;
import stoneframe.serena.storages.JsonConverter;
import stoneframe.serena.storages.SharedPreferencesStorage;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.timeservices.RealTimeService;

public class GlobalState extends Application
{
    private static GlobalState instance;

    private Routine<?> activeRoutine;
    private Chore activeChore;
    private Task activeTask;
    private Reminder activeReminder;
    private Checklist activeChecklist;
    private Balancer activeBalancer;
    private NoteView activeNote;
    private NoteGroupView activeNoteGroup;

    private Serena serena;

    @Override
    public synchronized void onCreate()
    {
        super.onCreate();

        if (instance == null)
        {
            instance = this;
        }
    }

    public static GlobalState getInstance()
    {
        return instance;
    }

    public Serena getSerena()
    {
        return serena;
    }

    @MainThread
    @NonNull
    public Serena getOrLoadSerena()
    {
        if (serena == null)
        {
            Serena loadedSerena = createSerena();
            loadedSerena.load();
            serena = loadedSerena;
        }

        return serena;
    }

    private Serena createSerena()
    {
        Storage storage = new SharedPreferencesStorage(
            this,
            new JsonConverter(
                new SimpleChoreSelectorConverter(),
                new WeeklyEffortTrackerConverter()));

        return new Serena(
            storage,
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());
    }

    public void setSerena(Serena serena)
    {
        this.serena = serena;
    }

    public Routine<?> getActiveRoutine()
    {
        return activeRoutine;
    }

    public void setActiveRoutine(Routine<?> activeRoutine)
    {
        this.activeRoutine = activeRoutine;
    }

    public Chore getActiveChore()
    {
        return activeChore;
    }

    public void setActiveChore(Chore activeChore)
    {
        this.activeChore = activeChore;
    }

    public Task getActiveTask()
    {
        return activeTask;
    }

    public void setActiveTask(Task activeTask)
    {
        this.activeTask = activeTask;
    }

    public Checklist getActiveChecklist()
    {
        return activeChecklist;
    }

    public void setActiveChecklist(Checklist activeChecklist)
    {
        this.activeChecklist = activeChecklist;
    }

    public Balancer getActiveBalancer()
    {
        return activeBalancer;
    }

    public void setActiveBalancer(Balancer activeBalancer)
    {
        this.activeBalancer = activeBalancer;
    }

    public NoteView getActiveNote()
    {
        return activeNote;
    }

    public void setActiveNote(NoteView note)
    {
        activeNote = note;
    }

    public NoteGroupView getActiveNoteGroup()
    {
        return activeNoteGroup;
    }

    public void setActiveNoteGroup(NoteGroupView noteGroup)
    {
        activeNoteGroup = noteGroup;
    }

    public Reminder getActiveReminder()
    {
        return activeReminder;
    }

    public void setActiveReminder(Reminder reminder)
    {
        activeReminder = reminder;
    }
}
