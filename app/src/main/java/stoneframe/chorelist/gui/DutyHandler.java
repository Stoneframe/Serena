package stoneframe.chorelist.gui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import stoneframe.chorelist.model.Duty;
import stoneframe.chorelist.model.Schedule;

public class DutyHandler extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private Schedule schedule;

    public DutyHandler(Context context, int resource, Schedule schedule) {
        super(context, resource);

        this.schedule = schedule;
    }

    public void update() {
        clear();
        for (Duty duty : schedule.getDuties()) {
            add(duty);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

}
