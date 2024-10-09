package stoneframe.chorelist.gui.routines.util;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import org.joda.time.LocalDate;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.routines.Procedure;
import stoneframe.chorelist.model.routines.Day;
import stoneframe.chorelist.model.routines.Week;

public class WeekExpandableListAdaptor extends BaseExpandableListAdapter
{
    private final Context context;
    private final Week week;

    public WeekExpandableListAdaptor(Context context, Week week)
    {
        this.context = context;
        this.week = week;
    }

    @Override
    public int getGroupCount()
    {
        return 7;
    }

    @Override
    public int getChildrenCount(int listPosition)
    {
        return week.getWeekDay(listPosition + 1).getProcedures().size();
    }

    @Override
    public Object getGroup(int listPosition)
    {
        return week.getWeekDay(listPosition + 1);
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition)
    {
        return week.getWeekDay(listPosition + 1)
            .getProcedures()
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
        Day weekDay = (Day)getGroup(listPosition);

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater)this.context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            view = layoutInflater.inflate(R.layout.expandable_list_group, null);
        }

        TextView listTitleTextView = view.findViewById(R.id.listTitle);

        listTitleTextView.setTypeface(null, Typeface.BOLD);

        if (weekDay.isToday(LocalDate.now()))
        {
            listTitleTextView.setText(String.format("> %s <", weekDay.getName()));
        }
        else
        {
            listTitleTextView.setText(weekDay.getName());
        }

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

        TextView expandedListTextView = view.findViewById(R.id.expandedListItem);

        expandedListTextView.setText(
            String.format(
                "%s - %s",
                procedure.getTime().toString("HH.mm"),
                procedure.getDescription()));

        return view;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition)
    {
        return true;
    }
}
