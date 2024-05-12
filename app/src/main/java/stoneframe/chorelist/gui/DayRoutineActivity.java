package stoneframe.chorelist.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.DayRoutine;
import stoneframe.chorelist.model.Procedure;

public class DayRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private int action;

    private ChoreList choreList;

    private DayRoutine routine;

    private EditText nameEditText;
    private CheckBox enabledCheckBox;
    private ListView procedureListView;

    private ArrayAdapter<Procedure> procedureListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_day);

        GlobalState globalState = GlobalState.getInstance(this);

        choreList = globalState.getChoreList();
        routine = (DayRoutine)globalState.ActiveRoutine;

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        Button button = findViewById(R.id.removeButton);
        button.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        procedureListAdapter = new ArrayAdapter<>(
            getBaseContext(),
            android.R.layout.simple_list_item_1);

        procedureListAdapter.addAll(routine.getAllProcedures());

        nameEditText = findViewById(R.id.day_routine_name_edit);
        enabledCheckBox = findViewById(R.id.day_routine_enabled_checkbox);
        procedureListView = findViewById(R.id.procedures);
        procedureListView.setAdapter(procedureListAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
            editProcedure(position));
        procedureListView.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedure(position));

        nameEditText.setText(routine.getName());
        enabledCheckBox.setChecked(routine.isEnabled());
    }

    public void saveClick(View view)
    {
        if (enabledCheckBox.isChecked() && !routine.isEnabled())
        {
            choreList.resetRoutine(routine);
        }

        routine.setName(nameEditText.getText().toString());
        routine.setEnabled(enabledCheckBox.isChecked());

        Intent intent = new Intent();

        intent.putExtra("RESULT", ROUTINE_RESULT_SAVE);
        intent.putExtra("ACTION", action);

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
        ProcedureEditDialog.create(this, (time, description) ->
        {
            Procedure procedure = new Procedure(description, time);

            routine.addProcedure(procedure);

            procedureListAdapter.add(procedure);
            procedureListAdapter.sort(Procedure::compareTo);
        });
    }

    private void editProcedure(int position)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

        assert procedure != null;

        ProcedureEditDialog.edit(
            this,
            procedure.getTime(),
            procedure.getDescription(),
            (time, description) ->
            {
                Procedure newProcedure = new Procedure(description, time);

                routine.removeProcedure(procedure);
                routine.addProcedure(newProcedure);

                procedureListAdapter.clear();
                procedureListAdapter.addAll(routine.getAllProcedures());
            });
    }

    private boolean removeProcedure(int position)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

        routine.removeProcedure(procedure);
        procedureListAdapter.remove(procedure);

        return true;
    }
}
