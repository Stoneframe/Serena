package stoneframe.chorelist.gui;

import android.app.Application;

import stoneframe.chorelist.model.Schedule;

public class GlobalState extends Application {

    private Schedule schedule;

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

}
