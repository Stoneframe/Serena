package stoneframe.chorelist.gui;

import android.app.Application;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.routines.Routine;

public class GlobalState extends Application
{
    private static GlobalState instance;

    public Routine ActiveRoutine;

    private Checklist activeChecklist;

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

    public Checklist getActiveChecklist()
    {
        return activeChecklist;
    }

    public void setActiveChecklist(Checklist activeChecklist)
    {
        this.activeChecklist = activeChecklist;
    }
}
