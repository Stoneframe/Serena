package stoneframe.serena.gui;

import android.app.Application;

import stoneframe.serena.model.Serena;
import stoneframe.serena.model.checklists.Checklist;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.balancers.Balancer;
import stoneframe.serena.model.notes.Note;
import stoneframe.serena.model.routines.Routine;
import stoneframe.serena.model.tasks.Task;

public class GlobalState extends Application
{
    private static GlobalState instance;

    private Routine<?> activeRoutine;
    private Chore activeChore;
    private Task activeTask;
    private Checklist activeChecklist;
    private Balancer activeBalancer;
    private Note activeNote;

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

    public Note getActiveNote()
    {
        return activeNote;
    }

    public void setActiveNote(Note note)
    {
        activeNote = note;
    }
}
