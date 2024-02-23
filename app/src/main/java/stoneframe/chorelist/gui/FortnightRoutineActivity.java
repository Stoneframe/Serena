package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.FortnightRoutine;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Routine;

public class FortnightRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private FortnightRoutine routine;

    private EditText nameEditText;
    private EditText startDateEditText;

    private ExpandableListView week1ExpandableList;
    private WeekExpandableListAdaptor week1ExpandableListAdaptor;

    private ExpandableListView week2ExpandableList;
    private WeekExpandableListAdaptor week2ExpandableListAdaptor;

    private Spinner procedureWeekSpinner;
    private Spinner procedureDaySpinner;
    private EditText procedureTimeEditText;
    private EditText procedureDescriptionEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_fortnight);

        routine = (FortnightRoutine)GlobalState.getInstance(this).RoutineToEdit;

        Intent intent = getIntent();

        int action = intent.getIntExtra("ACTION", -1);

        Button button = findViewById(R.id.removeButton);
        button.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view1, year, month, dayOfMonth) ->
            {
                DateTime startDate = new DateTime(year, month + 1, dayOfMonth, 0, 0);
                startDateEditText.setText(startDate.toString("yyyy-MM-dd"));
            },
            routine.getStartDate().getYear(),
            routine.getStartDate().getMonthOfYear() - 1,
            routine.getStartDate().getDayOfMonth());

        nameEditText = findViewById(R.id.fortnight_routine_name_edit);
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
        procedureTimeEditText = findViewById(R.id.week_procedure_time);
        procedureTimeEditText.setInputType(InputType.TYPE_NULL);
        procedureTimeEditText.setOnClickListener(v -> showTimePicker());

        procedureWeekSpinner = findViewById(R.id.week_procedure_week);
        procedureWeekSpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            new String[]{"1", "2"}));

        procedureDaySpinner = findViewById(R.id.week_procedure_day_of_week);
        procedureDaySpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}));

        procedureDescriptionEditText = findViewById(R.id.week_procedure_description);

        nameEditText.setText(routine.getName());
    }

    private boolean editProcedureWeek1(int groupPosition, int childPosition)
    {
        editProcedure(week1ExpandableListAdaptor, groupPosition, childPosition);

        return true;
    }

    private boolean removeProcedureWeek1(int position)
    {
        removeProcedure(week1ExpandableList, position, week1ExpandableListAdaptor);

        return true;
    }

    private boolean editProcedureWeek2(int groupPosition, int childPosition)
    {
        editProcedure(week2ExpandableListAdaptor, groupPosition, childPosition);

        return true;
    }

    private boolean removeProcedureWeek2(int position)
    {
        removeProcedure(week2ExpandableList, position, week2ExpandableListAdaptor);

        return true;
    }

    private void editProcedure(
        WeekExpandableListAdaptor weekExpandableListAdaptor,
        int groupPosition,
        int childPosition)
    {
        Procedure procedure = (Procedure)weekExpandableListAdaptor.getChild(
            groupPosition,
            childPosition);

        Routine.Day weekDay = (Routine.Day)weekExpandableListAdaptor.getGroup(
            groupPosition);

        new TimePickerDialog(
            this,
            (view, hourOfDay, minute) ->
            {
                LocalTime time = new LocalTime(hourOfDay, minute);

                Procedure newProcedure = new Procedure(procedure.getDescription().trim(), time);

                weekDay.removeProcedure(procedure);
                weekDay.addProcedure(newProcedure);

                weekExpandableListAdaptor.notifyDataSetChanged();
            },
            procedure.getTime().getHourOfDay(),
            procedure.getTime().getMinuteOfHour(),
            true).show();
    }

    private void removeProcedure(
        ExpandableListView week1ExpandableList,
        int position,
        WeekExpandableListAdaptor week1ExpandableListAdaptor)
    {
        long packedPosition = week1ExpandableList.getExpandableListPosition(position);

        int itemType = ExpandableListView.getPackedPositionType(packedPosition);

        if (itemType == ExpandableListView.PACKED_POSITION_TYPE_CHILD)
        {
            int groupPosition = ExpandableListView.getPackedPositionGroup(packedPosition);
            int childPosition = ExpandableListView.getPackedPositionChild(packedPosition);

            Procedure procedure = (Procedure)week1ExpandableListAdaptor.getChild(
                groupPosition,
                childPosition);

            Routine.Day weekDay = (Routine.Day)week1ExpandableListAdaptor.getGroup(
                groupPosition);

            weekDay.removeProcedure(procedure);

            week1ExpandableListAdaptor.notifyDataSetChanged();
        }
    }

    private void showTimePicker()
    {
        new TimePickerDialog(
            this,
            (view, hourOfDay, minute) ->
            {
                LocalTime time = new LocalTime(hourOfDay, minute);

                procedureTimeEditText.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));
            },
            0,
            0,
            true).show();
    }

    public void saveClick(View view)
    {
        routine.setName(nameEditText.getText().toString().trim());
        routine.setStartDate(LocalDate.parse(startDateEditText.getText().toString()));

        Intent intent = new Intent();

        intent.putExtra("RESULT", ROUTINE_RESULT_SAVE);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelClick(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void removeClick(View view)
    {
        Intent intent = new Intent();

        intent.putExtra("RESULT", ROUTINE_RESULT_REMOVE);

        setResult(RESULT_OK, intent);
        finish();
    }

    @SuppressLint("SetTextI18n")
    public void addProcedureClick(View view)
    {
        int week = (int)procedureWeekSpinner.getSelectedItemId() + 1;
        int dayOfWeek = (int)procedureDaySpinner.getSelectedItemId() + 1;
        LocalTime time = LocalTime.parse(procedureTimeEditText.getText().toString());
        String description = procedureDescriptionEditText.getText().toString().trim();

        Procedure procedure = new Procedure(description, time);

        routine.getWeek(week).getWeekDay(dayOfWeek).addProcedure(procedure);

        week1ExpandableListAdaptor.notifyDataSetChanged();
        week2ExpandableListAdaptor.notifyDataSetChanged();

        procedureTimeEditText.setText("00:00");
        procedureDescriptionEditText.setText("");
    }
}
