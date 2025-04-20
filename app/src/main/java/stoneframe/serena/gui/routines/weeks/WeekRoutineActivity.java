package stoneframe.serena.gui.routines.weeks;

import android.app.AlertDialog;
import android.widget.ExpandableListView;

import java.util.List;

import stoneframe.serena.R;
import stoneframe.serena.gui.routines.EditRoutineActivity;
import stoneframe.serena.gui.routines.util.WeekExpandableListAdaptor;
import stoneframe.serena.routines.Procedure;
import stoneframe.serena.routines.WeekRoutine;
import stoneframe.serena.routines.WeekRoutineEditor;

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
            removeOrCopy(position));

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
        WeekProcedureEditDialog.createProcedure(this, (createdProcedure, dayOfWeek) ->
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

        WeekProcedureEditDialog.editProcedure(
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

    private boolean removeOrCopy(int position)
    {
        long packedPosition = weekExpandableList.getExpandableListPosition(position);

        int itemType = ExpandableListView.getPackedPositionType(packedPosition);

        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            copyWeekDay(packedPosition);
        }
        else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            copyOrRemoveProcedure(packedPosition);
        }

        return true;
    }

    private void copyWeekDay(long packedPosition)
    {
        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Select option")
            .setCancelable(false)
            .setNegativeButton(
                "Copy",
                (dialog, copyButtonId) -> copyWeekDayProcedures(groupPosition + 1))
            .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void copyWeekDayProcedures(int dayOfWeek)
    {
        WeekProcedureEditDialog.copyWeekDay(
            this,
            dayOfWeek,
            (targetDayOfWeek) ->
            {
                List<Procedure> proceduresToCopy = routineEditor.getProcedures(dayOfWeek);

                proceduresToCopy.forEach(
                    p -> routineEditor.addProcedure(targetDayOfWeek, p.copy()));
            }
        );
    }

    private void copyOrRemoveProcedure(long packedPosition)
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

    private void removeProcedure(int weekDay, Procedure procedure)
    {
        routineEditor.removeProcedure(weekDay, procedure);
    }

    private void copyProcedure(int weekDay, Procedure procedure)
    {
        WeekProcedureEditDialog.copyProcedure(
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
