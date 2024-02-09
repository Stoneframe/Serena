package stoneframe.chorelist.gui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.WeekRoutine;

public class WeekRoutineListAdaptor extends BaseExpandableListAdapter
{
    private final Context context;
    private final WeekRoutine choreList;

    public WeekRoutineListAdaptor(Context context, WeekRoutine choreList)
    {
        this.context = context;
        this.choreList = choreList;
    }

    @Override
    public int getGroupCount()
    {
        return choreList.getAllRoutines().size();
    }

    @Override
    public int getChildrenCount(int listPosition)
    {
        return choreList.getAllRoutines().get(listPosition).getAllProcedures().size();
    }

    @Override
    public Object getGroup(int listPosition)
    {
        return choreList.getAllRoutines().get(listPosition);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition)
    {
        return choreList.getAllRoutines()
            .get(listPosition)
            .getAllProcedures()
            .get(expandedListPosition);
    }

    @Override
    public long getGroupId(int listPosition)
    {
        return getGroup(listPosition).hashCode();
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition)
    {
        return getChild(listPosition, expandedListPosition).hashCode();
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded, View view, ViewGroup viewGroup)
    {
        Routine routine = (Routine)getGroup(listPosition);

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)this.context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.expandable_list_group, null);
        }

        TextView listTitleTextView = (TextView)view.findViewById(R.id.listTitle);

        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(routine.getName());

        return view;
    }

    @Override
    public View getChildView(
        int listPosition,
        int expandedListPosition,
        boolean isExpanded,
        View view,
        ViewGroup viewGroup)
    {
        Procedure procedure = (Procedure)getChild(listPosition, expandedListPosition);

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.expandable_list_item, null);
        }

        TextView expandedListTextView = (TextView)view.findViewById(R.id.expandedListItem);

        expandedListTextView.setText(procedure.getDescription());

        return view;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition)
    {
        return true;
    }
}
