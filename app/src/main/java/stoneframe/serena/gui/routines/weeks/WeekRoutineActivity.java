package stoneframe.serena.gui.routines.weeks;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.routines.util.WeekExpandableListAdaptor;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.EditTextButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.routines.Procedure;
import stoneframe.serena.model.routines.Routine;
import stoneframe.serena.model.routines.WeekRoutine;

public class WeekRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private int action;

    private WeekRoutine routine;

    private Serena serena;

    private EditText nameEditText;
    private CheckBox enabledCheckBox;

    private ExpandableListView weekExpandableList;
    private WeekExpandableListAdaptor weekExpandableListAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_week);

        GlobalState globalState = GlobalState.getInstance();

        serena = globalState.getSerena();
        routine = (WeekRoutine)globalState.ActiveRoutine;

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        Button removeButton = findViewById(R.id.removeButton);
        removeButton.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        Button saveButton = findViewById(R.id.saveButton);

        nameEditText = findViewById(R.id.week_routine_name_edit);
        enabledCheckBox = findViewById(R.id.week_routine_enabled_checkbox);

        weekExpandableListAdaptor = new WeekExpandableListAdaptor(this, routine.getWeek());

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

        nameEditText.setText(routine.getName());
        enabledCheckBox.setChecked(routine.isEnabled());

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(nameEditText, EditTextCriteria.IS_NOT_EMPTY));
    }

    public void saveClick(View view)
    {
        if (enabledCheckBox.isChecked() && !routine.isEnabled())
        {
            serena.resetRoutine(routine);
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
        DialogUtils.showConfirmationDialog(
            this,
            "Remove Routine",
            "Are you sure you want to remove the routine?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                Intent intent = new Intent();

                intent.putExtra("RESULT", ROUTINE_RESULT_REMOVE);

                setResult(RESULT_OK, intent);
                finish();
            });
    }

    public void addProcedureClick(View view)
    {
        WeekProcedureEditDialog.create(this, (createdProcedure, dayOfWeek) ->
        {
            routine.getWeekDay(dayOfWeek).addProcedure(createdProcedure);

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
                Routine.Day oldWeekDay = routine.getWeekDay(groupPosition + 1);
                Routine.Day newWeekDay = routine.getWeekDay(dayOfWeek);

                oldWeekDay.removeProcedure(procedure);
                newWeekDay.addProcedure(editedProcedure);

                weekExpandableListAdaptor.notifyDataSetChanged();
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

            Routine.Day weekDay = (Routine.Day)weekExpandableListAdaptor.getGroup(
                groupPosition);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select option")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, removeButtonId) ->
                    removeProcedure(weekDay, procedure))
                .setNegativeButton("Copy", (dialog, copyButtonId) ->
                    copyProcedure(procedure, groupPosition))
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }

        return true;
    }

    private void removeProcedure(Routine.Day weekDay, Procedure procedure)
    {
        weekDay.removeProcedure(procedure);

        weekExpandableListAdaptor.notifyDataSetChanged();
    }

    private void copyProcedure(Procedure procedure, int dayOfWeek)
    {
        WeekProcedureEditDialog.copy(
            this,
            procedure,
            dayOfWeek,
            (copiedProcedure, copiedDayOfWeek) ->
            {
                routine.getWeekDay(copiedDayOfWeek).addProcedure(copiedProcedure);

                weekExpandableListAdaptor.notifyDataSetChanged();
                weekExpandableList.expandGroup(dayOfWeek - 1);
            });
    }
}
