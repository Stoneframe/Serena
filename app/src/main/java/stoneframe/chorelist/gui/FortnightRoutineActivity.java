package stoneframe.chorelist.gui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.HashMap;
import java.util.Map;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.FortnightRoutine;
import stoneframe.chorelist.model.Procedure;

public class FortnightRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private FortnightRoutine routine;

    private Map<Integer, ArrayAdapter<Procedure>> week1ProcedureListAdapters;
    private Map<Integer, ListView> week1ProcedureLists;

    private Map<Integer, ArrayAdapter<Procedure>> week2ProcedureListAdapters;
    private Map<Integer, ListView> week2ProcedureLists;

    private EditText nameEditText;
    private EditText startDateEditText;

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

        week1ProcedureListAdapters = new HashMap<>();
        week1ProcedureLists = new HashMap<>();

        week2ProcedureListAdapters = new HashMap<>();
        week2ProcedureLists = new HashMap<>();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (DatePickerDialog.OnDateSetListener)(view1, year, month, dayOfMonth) ->
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
        startDateEditText.setOnClickListener(view -> datePickerDialog.show());

        setupDay(R.id.procedures_monday_week1, 1, DateTimeConstants.MONDAY);
        setupDay(R.id.procedures_tuesday_week1, 1, DateTimeConstants.TUESDAY);
        setupDay(R.id.procedures_wednesday_week1, 1, DateTimeConstants.WEDNESDAY);
        setupDay(R.id.procedures_thursday_week1, 1, DateTimeConstants.THURSDAY);
        setupDay(R.id.procedures_friday_week1, 1, DateTimeConstants.FRIDAY);
        setupDay(R.id.procedures_saturday_week1, 1, DateTimeConstants.SATURDAY);
        setupDay(R.id.procedures_sunday_week1, 1, DateTimeConstants.SUNDAY);

        setupDay(R.id.procedures_monday_week2, 2, DateTimeConstants.MONDAY);
        setupDay(R.id.procedures_tuesday_week2, 2, DateTimeConstants.TUESDAY);
        setupDay(R.id.procedures_wednesday_week2, 2, DateTimeConstants.WEDNESDAY);
        setupDay(R.id.procedures_thursday_week2, 2, DateTimeConstants.THURSDAY);
        setupDay(R.id.procedures_friday_week2, 2, DateTimeConstants.FRIDAY);
        setupDay(R.id.procedures_saturday_week2, 2, DateTimeConstants.SATURDAY);
        setupDay(R.id.procedures_sunday_week2, 2, DateTimeConstants.SUNDAY);

        procedureTimeEditText = findViewById(R.id.week_procedure_time);
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

    private void setupDay(int procedureDayList, int week, int dayOfWeek)
    {
        ArrayAdapter<Procedure> adapter = createProcedureListAdapter(week, dayOfWeek);

        ListView list = setupProcedureListView(adapter, procedureDayList, dayOfWeek);

        getProcedureListAdapters(week).put(dayOfWeek, adapter);
        getProcedureLists(week).put(dayOfWeek, list);
    }

    private ListView setupProcedureListView(
        ArrayAdapter<Procedure> procedureArrayAdapter,
        int procedureList,
        int dayOfWeek)
    {
        ListView procedureListView = findViewById(procedureList);

        procedureListView.setAdapter(procedureArrayAdapter);
        procedureListView.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedure(position, procedureArrayAdapter, dayOfWeek));

        return procedureListView;
    }

    @NonNull
    private Map<Integer, ArrayAdapter<Procedure>> getProcedureListAdapters(int week)
    {
        switch (week)
        {
            case 1:
                return week1ProcedureListAdapters;
            case 2:
                return week2ProcedureListAdapters;
            default:
                throw new IllegalArgumentException();
        }
    }

    private Map<Integer, ListView> getProcedureLists(int week)
    {
        switch (week)
        {
            case 1:
                return week1ProcedureLists;
            case 2:
                return week2ProcedureLists;
            default:
                throw new IllegalArgumentException();
        }
    }

    @NonNull
    private ArrayAdapter<Procedure> createProcedureListAdapter(int week, int dayOfWeek)
    {
        return new ArrayAdapter<>(
            getBaseContext(),
            android.R.layout.simple_list_item_1,
            routine.getWeek(week).getWeekDay(dayOfWeek).getProcedures());
    }

    private boolean removeProcedure(
        int position,
        ArrayAdapter<Procedure> procedureListAdapter,
        int dayOfWeek)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

        if (routine.getWeek1().getProcedures().contains(procedure))
        {
            routine.getWeek1().getWeekDay(dayOfWeek).removeProcedure(procedure);
        }

        if (routine.getWeek2().getProcedures().contains(procedure))
        {
            routine.getWeek2().getWeekDay(dayOfWeek).removeProcedure(procedure);
        }

        procedureListAdapter.remove(procedure);

        return true;
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
        routine.setName(nameEditText.getText().toString());
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

    public void addProcedureClick(View view)
    {
        int week = (int)procedureWeekSpinner.getSelectedItemId() + 1;
        int dayOfWeek = (int)procedureDaySpinner.getSelectedItemId() + 1;
        LocalTime time = LocalTime.parse(procedureTimeEditText.getText().toString());
        String description = procedureDescriptionEditText.getText().toString();

        Procedure procedure = new Procedure(description, time);

        routine.getWeek(week).getWeekDay(dayOfWeek).addProcedure(procedure);

        getProcedureListAdapters(week).get(dayOfWeek).add(procedure);
        getProcedureListAdapters(week).get(dayOfWeek).sort(Procedure::compareTo);

//        procedureWeekSpinner.setSelection(0);
//        procedureDaySpinner.setSelection(0);
        procedureTimeEditText.setText("00:00");
        procedureDescriptionEditText.setText("");
    }
}
