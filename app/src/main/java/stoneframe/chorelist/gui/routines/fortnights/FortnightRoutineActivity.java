package stoneframe.chorelist.gui.routines.fortnights;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.ExpandableListView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.routines.RoutineActivity;
import stoneframe.chorelist.gui.routines.util.WeekExpandableListAdaptor;
import stoneframe.chorelist.model.routines.Day;
import stoneframe.chorelist.model.routines.FortnightRoutine;
import stoneframe.chorelist.model.routines.Procedure;
import stoneframe.chorelist.model.routines.Week;

public class FortnightRoutineActivity extends RoutineActivity<FortnightRoutine>
{
    private EditText startDateEditText;

    private ExpandableListView week1ExpandableList;
    private WeekExpandableListAdaptor week1ExpandableListAdaptor;

    private ExpandableListView week2ExpandableList;
    private WeekExpandableListAdaptor week2ExpandableListAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view1, year, month, dayOfMonth) ->
            {
                DateTime startDate = new DateTime(year, month + 1, dayOfMonth, 0, 0);
                startDateEditText.setText(startDate.toString("yyyy-MM-dd"));

                routine.setStartDate(LocalDate.parse(startDateEditText.getText().toString()));

                week1ExpandableListAdaptor.notifyDataSetChanged();
                week2ExpandableListAdaptor.notifyDataSetChanged();
            },
            routine.getStartDate().getYear(),
            routine.getStartDate().getMonthOfYear() - 1,
            routine.getStartDate().getDayOfMonth());

        startDateEditText = findViewById(R.id.fortnight_routine_start_date_edit);
        startDateEditText.setText(routine.getStartDate().toString("yyyy-MM-dd"));
        startDateEditText.setInputType(InputType.TYPE_NULL);
        startDateEditText.setOnClickListener(view -> datePickerDialog.show());

        week1ExpandableListAdaptor = new WeekExpandableListAdaptor(this, routine.getWeek1());
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

        week2ExpandableListAdaptor = new WeekExpandableListAdaptor(this, routine.getWeek2());
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
        FortnightProcedureEditDialog.create(this, (procedure, week, weekDay) ->
        {
            routine.getWeek(week).getWeekDay(weekDay).addProcedure(procedure);

            week1ExpandableListAdaptor.notifyDataSetChanged();
            week2ExpandableListAdaptor.notifyDataSetChanged();
        });
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

        FortnightProcedureEditDialog.edit(
            this,
            procedure,
            weekNumber,
            groupPosition,
            (editedProcedure, editedWeek, editedDayOfWeek) ->
            {
                Week oldWeek = routine.getWeek(weekNumber);
                Week newWeek = routine.getWeek(editedWeek);

                Day oldWeekDay = oldWeek.getWeekDay(groupPosition + 1);
                Day newWeekDay = newWeek.getWeekDay(editedDayOfWeek);

                oldWeekDay.removeProcedure(procedure);
                newWeekDay.addProcedure(editedProcedure);

                week1ExpandableListAdaptor.notifyDataSetChanged();
                week2ExpandableListAdaptor.notifyDataSetChanged();
            });
    }

    private void removeOrCopyProcedure(
        ExpandableListView weekExpandableList,
        int weekNumber,
        int position,
        WeekExpandableListAdaptor week1ExpandableListAdaptor)
    {
        long packedPosition = weekExpandableList.getExpandableListPosition(position);

        int itemType = ExpandableListView.getPackedPositionType(packedPosition);

        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

            Procedure procedure = (Procedure)week1ExpandableListAdaptor.getChild(
                groupPosition,
                childPosition);

            Day weekDay = (Day)week1ExpandableListAdaptor.getGroup(groupPosition);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select option")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, removeButtonId) ->
                    removeProcedure(weekDay, procedure))
                .setNegativeButton("Copy", (dialog, copyButtonId) ->
                    copyProcedure(procedure, weekNumber, groupPosition))
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void removeProcedure(Day weekDay, Procedure procedure)
    {
        weekDay.removeProcedure(procedure);

        week1ExpandableListAdaptor.notifyDataSetChanged();
        week2ExpandableListAdaptor.notifyDataSetChanged();
    }

    private void copyProcedure(Procedure procedure, int weekNumber, int dayOfWeek)
    {
        FortnightProcedureEditDialog.copy(
            this,
            procedure,
            weekNumber,
            dayOfWeek,
            (copiedProcedure, week, weekDay) ->
            {
                routine.getWeek(week).getWeekDay(weekDay).addProcedure(copiedProcedure);

                week1ExpandableListAdaptor.notifyDataSetChanged();
                week2ExpandableListAdaptor.notifyDataSetChanged();
            });
    }
}
