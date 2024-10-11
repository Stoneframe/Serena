package stoneframe.chorelist.gui;

import android.app.Application;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.limiters.Limiter;
import stoneframe.chorelist.model.routines.Routine;
import stoneframe.chorelist.model.tasks.Task;

public class GlobalState extends Application
{
    private static GlobalState instance;

    private Routine<?> activeRoutine;
    private Chore activeChore;
    private Task activeTask;
    private Checklist activeChecklist;
    private Limiter activeLimiter;

    private ChoreList choreList;

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

    public ChoreList getChoreList()
    {
        return choreList;
    }

    public void setChoreList(ChoreList choreList)
    {
        this.choreList = choreList;
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

    public Limiter getActiveLimiter()
    {
        return activeLimiter;
    }

    public void setActiveLimiter(Limiter activeLimiter)
    {
        this.activeLimiter = activeLimiter;
    }
}
