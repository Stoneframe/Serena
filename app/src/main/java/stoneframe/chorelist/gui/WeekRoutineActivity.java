package stoneframe.chorelist.gui;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import java.util.HashMap;
import java.util.Map;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.WeekRoutine;

public class WeekRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private WeekRoutine routine;

    private Map<Integer, ArrayAdapter<Procedure>> procedureListAdapters;

    private EditText nameEditText;

    private WeekRoutineListAdaptor routineProcedureListAdaptor;

    private EditText procedureTimeEditText;
    private EditText procedureDescriptionEditText;
    private Spinner procedureDaySpinner;

    public void saveClick(View view)
    {
        routine.setName(nameEditText.getText().toString());

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
        LocalTime time = LocalTime.parse(procedureTimeEditText.getText().toString());
        String description = procedureDescriptionEditText.getText().toString();
        int dayOfWeek = (int)procedureDaySpinner.getSelectedItemId() + 1;

        Procedure procedure = new Procedure(description, time);

        routine.getWeekDay(dayOfWeek).addProcedure(procedure);

        ArrayAdapter<Procedure> procedureArrayAdapter = procedureListAdapters.get(dayOfWeek);

        procedureArrayAdapter.add(procedure);
        procedureArrayAdapter.sort(Procedure::compareTo);

        procedureTimeEditText.setText("00:00");
        procedureDescriptionEditText.setText("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_week);

        routine = (WeekRoutine)GlobalState.getInstance(this).RoutineToEdit;

        Intent intent = getIntent();

        int action = intent.getIntExtra("ACTION", -1);

        Button button = findViewById(R.id.removeButton);
        button.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        procedureListAdapters = new HashMap<>();

        nameEditText = findViewById(R.id.fortnight_routine_name_edit);

        routineProcedureListAdaptor = new WeekRoutineListAdaptor(
            this,
            GlobalState.getInstance(this).getChoreList());

        ExpandableListView routineProcedureList = findViewById(R.id.routine_procedure_list);
        routineProcedureList.setAdapter(routineProcedureListAdaptor);

        setupDay(R.id.procedures_monday, DateTimeConstants.MONDAY);
        setupDay(R.id.procedures_tuesday, DateTimeConstants.TUESDAY);
        setupDay(R.id.procedures_wednesday, DateTimeConstants.WEDNESDAY);
        setupDay(R.id.procedures_thursday, DateTimeConstants.THURSDAY);
        setupDay(R.id.procedures_friday, DateTimeConstants.FRIDAY);
        setupDay(R.id.procedures_saturday, DateTimeConstants.SATURDAY);
        setupDay(R.id.procedures_sunday, DateTimeConstants.SUNDAY);

        procedureTimeEditText = findViewById(R.id.week_procedure_time);
        procedureTimeEditText.setInputType(InputType.TYPE_NULL);
        procedureTimeEditText.setOnClickListener(v -> showTimePicker());

        procedureDaySpinner = findViewById(R.id.week_procedure_day_of_week);
        procedureDaySpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"}));

        procedureDescriptionEditText = findViewById(R.id.week_procedure_description);

        nameEditText.setText(routine.getName());
    }

    private void setupDay(int procedureDayList, int dayOfWeek)
    {
        ArrayAdapter<Procedure> adapter = createProcedureListAdapter(dayOfWeek);

        setupProcedureListView(adapter, procedureDayList, dayOfWeek);

        procedureListAdapters.put(dayOfWeek, adapter);
    }

    private void setupProcedureListView(
        ArrayAdapter<Procedure> procedureArrayAdapter,
        int procedureList,
        int dayOfWeek)
    {
        ListView procedureListView = findViewById(procedureList);

        procedureListView.setAdapter(procedureArrayAdapter);
        procedureListView.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedure(position, procedureArrayAdapter, dayOfWeek));
    }

    @NonNull
    private ArrayAdapter<Procedure> createProcedureListAdapter(int dayOfWeek)
    {
        return new ArrayAdapter<>(
            getBaseContext(),
            android.R.layout.simple_list_item_1,
            routine.getWeekDay(dayOfWeek).getProcedures());
    }

    private boolean removeProcedure(
        int position,
        ArrayAdapter<Procedure> procedureListAdapter,
        int dayOfWeek)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

        WeekRoutine.WeekDay weekDay = routine.getWeekDay(dayOfWeek);

        weekDay.removeProcedure(procedure);
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
}
