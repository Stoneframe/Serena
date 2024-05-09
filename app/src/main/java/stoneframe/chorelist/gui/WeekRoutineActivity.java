package stoneframe.chorelist.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.WeekRoutine;

public class WeekRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private WeekRoutine routine;

    private EditText nameEditText;

    private ExpandableListView weekExpandableList;
    private WeekExpandableListAdaptor weekExpandableListAdaptor;

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
        ProcedureEditDialog.create(this, (time, description, dayOfWeek) ->
        {
            Procedure procedure = new Procedure(description, time);

            routine.getWeekDay(dayOfWeek).addProcedure(procedure);

            weekExpandableListAdaptor.notifyDataSetChanged();
            weekExpandableList.expandGroup(dayOfWeek - 1);
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_week);

        routine = (WeekRoutine)GlobalState.getInstance(this).ActiveRoutine;

        Intent intent = getIntent();

        int action = intent.getIntExtra("ACTION", -1);

        Button removeRoutineButton = findViewById(R.id.removeButton);
        removeRoutineButton.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        nameEditText = findViewById(R.id.week_routine_name_edit);

        weekExpandableListAdaptor = new WeekExpandableListAdaptor(this, routine.getWeek());

        weekExpandableList = findViewById(R.id.week_procedure_list);
        weekExpandableList.setAdapter(weekExpandableListAdaptor);
        weekExpandableList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) ->
            editProcedure(groupPosition, childPosition));
        weekExpandableList.setOnItemLongClickListener((parent, view, position, id) ->
            removeProcedure(position));

        for (int i = 0; i < weekExpandableListAdaptor.getGroupCount(); i++)
        {
            weekExpandableList.expandGroup(i);
        }

        nameEditText.setText(routine.getName());
    }

    private boolean editProcedure(int groupPosition, int childPosition)
    {
        Procedure procedure = (Procedure)weekExpandableListAdaptor.getChild(
            groupPosition,
            childPosition);

        Routine.Day weekDay = (Routine.Day)weekExpandableListAdaptor.getGroup(
            groupPosition);

        ProcedureEditDialog.edit(
            this,
            procedure.getTime(),
            procedure.getDescription(),
            (time, description) ->
            {
                Procedure newProcedure = new Procedure(description, time);

                weekDay.removeProcedure(procedure);
                weekDay.addProcedure(newProcedure);

                weekExpandableListAdaptor.notifyDataSetChanged();
            });

        return true;
    }

    private boolean removeProcedure(int position)
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

            Routine.Day weekDay = (Routine.Day)weekExpandableListAdaptor.getGroup(
                groupPosition);

            weekDay.removeProcedure(procedure);

            weekExpandableListAdaptor.notifyDataSetChanged();
        }

        return true;
    }
}
