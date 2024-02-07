package stoneframe.chorelist.gui;

import android.app.Activity;
import android.app.Application;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.model.DayRoutine;
import stoneframe.chorelist.model.Routine;

public class GlobalState extends Application
{
    public Routine RoutineToEdit;

    private ChoreList choreList;

    public static GlobalState getInstance(Activity activity)
    {
        return (GlobalState)Objects.requireNonNull(activity).getApplication();
    }

    public ChoreList getChoreList()
    {
        return choreList;
    }

    public void setChoreList(ChoreList choreList)
    {
        this.choreList = choreList;
    }
}
