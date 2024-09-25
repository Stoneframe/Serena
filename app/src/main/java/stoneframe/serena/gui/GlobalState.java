package stoneframe.serena.gui;

import android.app.Application;

import stoneframe.serena.model.Serena;
import stoneframe.serena.model.checklists.Checklist;
import stoneframe.serena.model.limiters.Limiter;
import stoneframe.serena.model.routines.Routine;

public class GlobalState extends Application
{
    private static GlobalState instance;

    public Routine ActiveRoutine;

    private Checklist activeChecklist;
    private Limiter activeLimiter;

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

    public void Serena(Serena serena)
    {
        this.serena = serena;
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
