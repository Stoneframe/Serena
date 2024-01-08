package stoneframe.chorelist.gui;

import android.app.Application;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.model.ChoreManager;

public class GlobalState extends Application
{
    private ChoreList choreList;

    public ChoreList getChoreList()
    {
        return choreList;
    }

    public void setChoreList(ChoreList choreList)
    {
        this.choreList = choreList;
    }
}
