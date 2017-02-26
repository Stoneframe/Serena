package stoneframe.chorelist.gui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class MenuHandler extends ArrayAdapter implements AdapterView.OnItemClickListener {

    private List<List<View>> views;

    public MenuHandler(Context context, int resource) {
        super(context, resource, new String[]{"ToDo List", "Schedule"});

        views = new ArrayList<>();
    }

    public void addView(int position, View view) {
        if (position >= views.size() || views.get(position) == null) {
            views.add(position, new ArrayList<View>());
        }
        views.get(position).add(view);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        for (List<View> l : views) {
            for (View v : l) {
                v.setVisibility(View.INVISIBLE);
            }
        }
        if (position < views.size()) {
            for (View v : views.get(position)) {
                v.setVisibility(View.VISIBLE);
            }
        }
    }

}
