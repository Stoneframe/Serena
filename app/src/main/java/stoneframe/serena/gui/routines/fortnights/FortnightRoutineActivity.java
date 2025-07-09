package stoneframe.serena.gui.routines.fortnights;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ExpandableListView;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;

import stoneframe.serena.R;
import stoneframe.serena.gui.routines.EditRoutineActivity;
import stoneframe.serena.gui.routines.util.WeekExpandableListAdaptor;
import stoneframe.serena.routines.FortnightRoutine;
import stoneframe.serena.routines.FortnightRoutineEditor;
import stoneframe.serena.routines.Procedure;

public class FortnightRoutineActivity extends EditRoutineActivity<FortnightRoutine, FortnightRoutineEditor, FortnightRoutineEditor.FortnightRoutineEditorListener>
{
    private final FortnightRoutineEditorListener listener = new FortnightRoutineEditorListener();

    private EditText startDateEditText;

    private ExpandableListView week1ExpandableList;
    private WeekExpandableListAdaptor week1ExpandableListAdaptor;

    private ExpandableListView week2ExpandableList;
    private WeekExpandableListAdaptor week2ExpandableListAdaptor;

    @Override
    protected String getActivityTitle()
    {
        return "Fortnight Routine";
    }

    @Override
    protected FortnightRoutineEditor getRoutineEditor(FortnightRoutine routine)
    {
        return serena.getRoutineManager().getFortnightRoutineEditor(routine);
    }

    @Override
    protected void startActivity()
    {
        routineEditor.addListener(listener);
    }

    @Override
    protected void stopActivity()
    {
        routineEditor.removeListener(listener);
    }

    @Override
    protected void createSpecialisedActivity()
    {
        DatePickerDialog datePickerDialog = getDatePickerDialog();

        startDateEditText = findViewById(R.id.fortnight_routine_start_date_edit);
        startDateEditText.setText(routineEditor.getStartDate().toString("yyyy-MM-dd"));
        startDateEditText.setInputType(InputType.TYPE_NULL);
        startDateEditText.setOnClickListener(view -> datePickerDialog.show());

        week1ExpandableListAdaptor = new WeekExpandableListAdaptor(this, routineEditor.getWeek1());
        week1ExpandableList = findViewById(R.id.week1_procedure_list);
        week1ExpandableList.setAdapter(week1ExpandableListAdaptor);
        week1ExpandableList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) ->
            editProcedureWeek1(groupPosition, childPosition));
        week1ExpandableList.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedureWeek1(position));

        for (int i = 0; i < week1ExpandableListAdaptor.getGroupCount(); i++)
        {
            week1ExpandableList.expandGroup(i);
        }

        week2ExpandableListAdaptor = new WeekExpandableListAdaptor(this, routineEditor.getWeek2());
        week2ExpandableList = findViewById(R.id.week2_procedure_list);
        week2ExpandableList.setAdapter(week2ExpandableListAdaptor);
        week2ExpandableList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) ->
            editProcedureWeek2(groupPosition, childPosition));
        week2ExpandableList.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedureWeek2(position));

        for (int i = 0; i < week2ExpandableListAdaptor.getGroupCount(); i++)
        {
            week2ExpandableList.expandGroup(i);
        }
    }

    @Override
    protected int getRoutineContentView()
    {
        return R.layout.activity_routine_fortnight;
    }

    @Override
    protected void addProcedure()
    {
        FortnightProcedureEditDialog.createProcedure(this, (procedure, week, weekDay) ->
            routineEditor.addProcedure(week, weekDay, procedure));
    }

    private @NonNull DatePickerDialog getDatePickerDialog()
    {
        LocalDate currentStartDate = routineEditor.getStartDate();

        return new DatePickerDialog(
            this,
            (view1, year, month, dayOfMonth) ->
            {
                DateTime startDate = new DateTime(year, month + 1, dayOfMonth, 0, 0);
                startDateEditText.setText(startDate.toString("yyyy-MM-dd"));

                routineEditor.setStartDate(LocalDate.parse(startDateEditText.getText().toString()));
            },
            currentStartDate.getYear(),
            currentStartDate.getMonthOfYear() - 1,
            currentStartDate.getDayOfMonth());
    }

    private boolean editProcedureWeek1(int groupPosition, int childPosition)
    {
        editProcedure(week1ExpandableListAdaptor, 1, groupPosition, childPosition);

        return true;
    }

    private boolean removeProcedureWeek1(int position)
    {
        removeOrCopyProcedure(week1ExpandableList, 1, position, week1ExpandableListAdaptor);

        return true;
    }

    private boolean editProcedureWeek2(int groupPosition, int childPosition)
    {
        editProcedure(week2ExpandableListAdaptor, 2, groupPosition, childPosition);

        return true;
    }

    private boolean removeProcedureWeek2(int position)
    {
        removeOrCopyProcedure(week2ExpandableList, 2, position, week2ExpandableListAdaptor);

        return true;
    }

    private void editProcedure(
        WeekExpandableListAdaptor weekExpandableListAdaptor,
        int weekNumber,
        int groupPosition,
        int childPosition)
    {
        Procedure procedure = (Procedure)weekExpandableListAdaptor.getChild(
            groupPosition,
            childPosition);

        FortnightProcedureEditDialog.editProcedure(
            this,
            procedure,
            weekNumber,
            groupPosition,
            (editedProcedure, editedWeek, editedDayOfWeek) ->
            {
                routineEditor.removeProcedure(weekNumber, groupPosition + 1, procedure);
                routineEditor.addProcedure(editedWeek, editedDayOfWeek, editedProcedure);
            });
    }

    private void removeOrCopyProcedure(
        ExpandableListView weekExpandableList,
        int weekNumber,
        int position,
        WeekExpandableListAdaptor weekExpandableListAdaptor)
    {
        long packedPosition = weekExpandableList.getExpandableListPosition(position);

        int itemType = ExpandableListView.getPackedPositionType(packedPosition);

        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_GROUP)
        {
            copyWeekDay(weekNumber, packedPosition);
        }
        else if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            copyOrRemoveProcedure(weekNumber, weekExpandableListAdaptor, packedPosition);
        }
    }

    private void copyWeekDay(int weekNumber, long packedPosition)
    {
        int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Select option")
            .setCancelable(false)
            .setNegativeButton("Copy", (dialog, copyButtonId) ->
                copyWeekDay(weekNumber, groupPosition + 1))
            .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void copyWeekDay(int week, int dayOfWeek)
    {
        FortnightProcedureEditDialog.copyWeekDay(
            this,
            week,
            dayOfWeek,
            (targetWeek, targetWeekDay) ->
            {
                List<Procedure> proceduresToCopy = routineEditor.getProcedures(week, dayOfWeek);

                proceduresToCopy.forEach(
                    p -> routineEditor.addProcedure(targetWeek, targetWeekDay, p.copy()));
            });
    }

    private void copyOrRemoveProcedure(
        int weekNumber,
        WeekExpandableListAdaptor weekExpandableListAdaptor,
        long packedPosition)
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
                removeProcedure(weekNumber, groupPosition + 1, procedure))
            .setNegativeButton("Copy", (dialog, copyButtonId) ->
                copyProcedure(procedure, weekNumber, groupPosition))
            .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void removeProcedure(int week, int weekDay, Procedure procedure)
    {
        routineEditor.removeProcedure(week, weekDay, procedure);
    }

    private void copyProcedure(Procedure procedure, int weekNumber, int dayOfWeek)
    {
        FortnightProcedureEditDialog.copyProcedure(
            this,
            procedure,
            weekNumber,
            dayOfWeek,
            (copiedProcedure, week, weekDay) -> routineEditor.addProcedure(
                week,
                weekDay,
                copiedProcedure));
    }

    private class FortnightRoutineEditorListener implements FortnightRoutineEditor.FortnightRoutineEditorListener
    {
        @Override
        public void nameChanged()
        {
        }

        @Override
        public void isEnabledChanged()
        {
        }

        @Override
        public void startDateChanged()
        {
        }

        @Override
        public void procedureAdded()
        {
            week1ExpandableListAdaptor.notifyDataSetChanged();
            week2ExpandableListAdaptor.notifyDataSetChanged();
        }

        @Override
        public void procedureRemoved()
        {
            week1ExpandableListAdaptor.notifyDataSetChanged();
            week2ExpandableListAdaptor.notifyDataSetChanged();
        }
    }
}
