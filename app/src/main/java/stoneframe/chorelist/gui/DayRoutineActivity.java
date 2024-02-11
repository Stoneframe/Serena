package stoneframe.chorelist.gui;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.DayRoutine;
import stoneframe.chorelist.model.Procedure;

public class DayRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private DayRoutine routine;

    private EditText nameEditText;
    private ListView procedureListView;

    private EditText procedureTimeEditText;
    private EditText procedureDescriptionEditText;

    private ArrayAdapter<Procedure> procedureListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_day);

        routine = (DayRoutine)GlobalState.getInstance(this).RoutineToEdit;

        Intent intent = getIntent();

        int action = intent.getIntExtra("ACTION", -1);

        Button button = findViewById(R.id.removeButton);
        button.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        procedureListAdapter = new ArrayAdapter<>(
            getBaseContext(),
            android.R.layout.simple_list_item_1);

        procedureListAdapter.addAll(routine.getAllProcedures());

        nameEditText = findViewById(R.id.day_routine_name_edit);
        procedureListView = findViewById(R.id.procedures);
        procedureListView.setAdapter(procedureListAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
            editProcedure(position));
        procedureListView.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedure(position));

        procedureTimeEditText = findViewById(R.id.procedure_time);
        procedureTimeEditText.setInputType(InputType.TYPE_NULL);
        procedureTimeEditText.setOnClickListener(v ->
            new TimePickerDialog(
                this,
                (view, hourOfDay, minute) ->
                {
                    LocalTime time = new LocalTime(hourOfDay, minute);

                    procedureTimeEditText.setText(time.toString(DateTimeFormat.forPattern("HH:mm")));
                },
                0,
                0,
                true).show());

        procedureDescriptionEditText = findViewById(R.id.procedureDescription);

        nameEditText.setText(routine.getName());
    }

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

        Procedure procedure = new Procedure(description, time);

        routine.addProcedure(procedure);
        procedureListAdapter.add(procedure);
        procedureListAdapter.sort(Procedure::compareTo);

        procedureTimeEditText.setText("00:00");
        procedureDescriptionEditText.setText("");
    }

    private void editProcedure(int position)
    {
        Procedure procedure = (Procedure)procedureListAdapter.getItem(position);

        new TimePickerDialog(
            this,
            (view, hourOfDay, minute) ->
            {
                LocalTime time = new LocalTime(hourOfDay, minute);

                Procedure newProcedure = new Procedure(procedure.getDescription(), time);

                routine.removeProcedure(procedure);
                routine.addProcedure(newProcedure);

                procedureListAdapter.clear();
                procedureListAdapter.addAll(routine.getAllProcedures());
            },
            procedure.getTime().getHourOfDay(),
            procedure.getTime().getMinuteOfHour(),
            true).show();
    }

    private boolean removeProcedure(int position)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

        routine.removeProcedure(procedure);
        procedureListAdapter.remove(procedure);

        return true;
    }
}
