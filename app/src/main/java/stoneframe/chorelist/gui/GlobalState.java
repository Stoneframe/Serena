package stoneframe.chorelist.gui;

import android.app.Application;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.limiters.Limiter;
import stoneframe.chorelist.model.routines.Routine;

public class GlobalState extends Application
{
    private static GlobalState instance;

    private Checklist activeChecklist;
    private Routine activeRoutine;
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

    public Routine getActiveRoutine()
    {
        return activeRoutine;
    }

    public void setActiveRoutine(Routine activeRoutine)
    {
        this.activeRoutine = activeRoutine;
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
