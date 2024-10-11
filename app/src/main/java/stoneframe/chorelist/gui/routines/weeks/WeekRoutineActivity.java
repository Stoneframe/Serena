package stoneframe.chorelist.gui.routines.weeks;

import android.app.AlertDialog;
import android.widget.ExpandableListView;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.routines.EditRoutineActivity;
import stoneframe.chorelist.gui.routines.util.WeekExpandableListAdaptor;
import stoneframe.chorelist.model.routines.Day;
import stoneframe.chorelist.model.routines.Procedure;
import stoneframe.chorelist.model.routines.WeekRoutine;

public class WeekRoutineActivity extends EditRoutineActivity<WeekRoutine>
{
    private ExpandableListView weekExpandableList;
    private WeekExpandableListAdaptor weekExpandableListAdaptor;

    @Override
    protected String getActivityTitle()
    {
        return "Week Routine";
    }

    @Override
    protected void createSpecialisedActivity()
    {
        weekExpandableListAdaptor = new WeekExpandableListAdaptor(this, routine.getWeek());

        weekExpandableList = findViewById(R.id.week_procedure_list);
        weekExpandableList.setAdapter(weekExpandableListAdaptor);
        weekExpandableList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) ->
            editProcedure(groupPosition, childPosition));
        weekExpandableList.setOnItemLongClickListener((parent, view, position, id) ->
            removeOrCopyProcedure(position));

        for (int i = 0; i < weekExpandableListAdaptor.getGroupCount(); i++)
        {
            weekExpandableList.expandGroup(i);
        }
    }

    @Override
    protected int getRoutineContentView()
    {
        return R.layout.activity_routine_week;
    }

    @Override
    protected void addProcedure()
    {
        WeekProcedureEditDialog.create(this, (createdProcedure, dayOfWeek) ->
        {
            routine.getWeek().getWeekDay(dayOfWeek).addProcedure(createdProcedure);

            weekExpandableListAdaptor.notifyDataSetChanged();
            weekExpandableList.expandGroup(dayOfWeek - 1);
        });
    }

    private boolean editProcedure(int groupPosition, int childPosition)
    {
        Procedure procedure = (Procedure)weekExpandableListAdaptor.getChild(
            groupPosition,
            childPosition);

        WeekProcedureEditDialog.edit(
            this,
            procedure,
            groupPosition,
            (editedProcedure, dayOfWeek) ->
            {
                Day oldWeekDay = routine.getWeek().getWeekDay(groupPosition + 1);
                Day newWeekDay = routine.getWeek().getWeekDay(dayOfWeek);

                oldWeekDay.removeProcedure(procedure);
                newWeekDay.addProcedure(editedProcedure);

                weekExpandableListAdaptor.notifyDataSetChanged();
            });

        return true;
    }

    private boolean removeOrCopyProcedure(int position)
    {
        long packedPosition = weekExpandableList.getExpandableListPosition(position);

        int itemType = ExpandableListView.getPackedPositionType(packedPosition);

        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

            Procedure procedure = (Procedure)weekExpandableListAdaptor.getChild(
                groupPosition,
                childPosition);

            Day weekDay = (Day)weekExpandableListAdaptor.getGroup(
                groupPosition);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select option")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, removeButtonId) ->
                    removeProcedure(weekDay, procedure))
                .setNegativeButton("Copy", (dialog, copyButtonId) ->
                    copyProcedure(procedure, groupPosition))
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }

        return true;
    }

    private void removeProcedure(Day weekDay, Procedure procedure)
    {
        weekDay.removeProcedure(procedure);

        weekExpandableListAdaptor.notifyDataSetChanged();
    }

    private void copyProcedure(Procedure procedure, int dayOfWeek)
    {
        WeekProcedureEditDialog.copy(
            this,
            procedure,
            dayOfWeek,
            (copiedProcedure, copiedDayOfWeek) ->
            {
                routine.getWeek().getWeekDay(copiedDayOfWeek).addProcedure(copiedProcedure);

                weekExpandableListAdaptor.notifyDataSetChanged();
                weekExpandableList.expandGroup(copiedDayOfWeek - 1);
            });
    }
}
