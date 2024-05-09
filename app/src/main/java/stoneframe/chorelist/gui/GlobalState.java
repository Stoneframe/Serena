package stoneframe.chorelist.gui;

import android.app.Activity;
import android.app.Application;

import androidx.fragment.app.Fragment;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.model.Checklist;
import stoneframe.chorelist.model.Routine;

public class GlobalState extends Application
{
    public Routine ActiveRoutine;
    public Checklist ActiveChecklist;

    private ChoreList choreList;

    public static GlobalState getInstance(Activity activity)
    {
        return (GlobalState)Objects.requireNonNull(activity).getApplication();
    }

    public static GlobalState getInstance(Fragment fragment)
    {
        return (GlobalState)fragment.requireActivity().getApplication();
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
