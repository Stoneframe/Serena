package stoneframe.chorelist.gui;

import android.app.Application;

import stoneframe.chorelist.model.ChoreManager;

public class GlobalState extends Application
{
    private ChoreManager choreManager;

    public ChoreManager getSchedule()
    {
        return choreManager;
    }

    public void setSchedule(ChoreManager choreManager)
    {
        this.choreManager = choreManager;
    }
}
