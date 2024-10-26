package stoneframe.serena.gui.routines.weeks;

import android.app.AlertDialog;
import android.widget.ExpandableListView;

import stoneframe.serena.R;
import stoneframe.serena.gui.routines.EditRoutineActivity;
import stoneframe.serena.gui.routines.util.WeekExpandableListAdaptor;
import stoneframe.serena.model.routines.Procedure;
import stoneframe.serena.model.routines.WeekRoutine;
import stoneframe.serena.model.routines.WeekRoutineEditor;

public class WeekRoutineActivity extends EditRoutineActivity<WeekRoutine, WeekRoutineEditor> implements WeekRoutineEditor.WeekRoutineEditorListener
{
    private ExpandableListView weekExpandableList;
    private WeekExpandableListAdaptor weekExpandableListAdaptor;

    @Override
    public void nameChanged()
    {

    }

    @Override
    public void isEnabledChanged()
    {

    }

    @Override
    public void procedureAdded()
    {
        weekExpandableListAdaptor.notifyDataSetChanged();
    }

    @Override
    public void procedureRemoved()
    {
        weekExpandableListAdaptor.notifyDataSetChanged();
    }

    @Override
    protected String getActivityTitle()
    {
        return "Week Routine";
    }

    @Override
    protected WeekRoutineEditor getRoutineEditor(WeekRoutine routine)
    {
        return serena.getRoutineManager().getWeekRoutineEditor(routine);
    }

    @Override
    protected void createSpecialisedActivity()
    {
        weekExpandableListAdaptor = new WeekExpandableListAdaptor(this, routineEditor.getWeek());

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
    protected void onStart()
    {
        super.onStart();

        routineEditor.addListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        routineEditor.removeListener(this);
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
            routineEditor.addProcedure(dayOfWeek, createdProcedure);

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
                routineEditor.removeProcedure(groupPosition + 1, procedure);
                routineEditor.addProcedure(dayOfWeek, editedProcedure);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select option")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, removeButtonId) ->
                    removeProcedure(groupPosition + 1, procedure))
                .setNegativeButton("Copy", (dialog, copyButtonId) ->
                    copyProcedure(groupPosition, procedure))
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }

        return true;
    }

    private void removeProcedure(int weekDay, Procedure procedure)
    {
        routineEditor.removeProcedure(weekDay, procedure);
    }

    private void copyProcedure(int weekDay, Procedure procedure)
    {
        WeekProcedureEditDialog.copy(
            this,
            procedure,
            weekDay,
            (copiedProcedure, copiedDayOfWeek) ->
            {
                routineEditor.addProcedure(copiedDayOfWeek, copiedProcedure);
                weekExpandableList.expandGroup(copiedDayOfWeek - 1);
            });
    }
}
