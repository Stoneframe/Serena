package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.routines.FortnightRoutine;
import stoneframe.chorelist.model.routines.Procedure;
import stoneframe.chorelist.model.routines.Routine;

public class FortnightRoutineActivity extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    public static final int ROUTINE_RESULT_SAVE = 0;
    public static final int ROUTINE_RESULT_REMOVE = 1;

    private int action;

    private FortnightRoutine routine;

    private ChoreList choreList;

    private EditText nameEditText;
    private CheckBox enabledCheckBox;
    private EditText startDateEditText;

    private ExpandableListView week1ExpandableList;
    private WeekExpandableListAdaptor week1ExpandableListAdaptor;

    private ExpandableListView week2ExpandableList;
    private WeekExpandableListAdaptor week2ExpandableListAdaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_fortnight);

        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();
        routine = (FortnightRoutine)globalState.ActiveRoutine;

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        Button removeButton = findViewById(R.id.removeButton);
        removeButton.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        Button saveButton = findViewById(R.id.saveButton);

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
        enabledCheckBox = findViewById(R.id.fortnight_routine_enabled_checkbox);
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

        nameEditText.setText(routine.getName());
        enabledCheckBox.setChecked(routine.isEnabled());

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(nameEditText, EditTextCriteria.IS_NOT_EMPTY));
    }

    private boolean editProcedureWeek1(int groupPosition, int childPosition)
    {
        editProcedure(week1ExpandableListAdaptor, groupPosition, childPosition);

        return true;
    }

    private boolean removeProcedureWeek1(int position)
    {
        removeOrCopyProcedure(week1ExpandableList, position, week1ExpandableListAdaptor);

        return true;
    }

    private boolean editProcedureWeek2(int groupPosition, int childPosition)
    {
        editProcedure(week2ExpandableListAdaptor, groupPosition, childPosition);

        return true;
    }

    private boolean removeProcedureWeek2(int position)
    {
        removeOrCopyProcedure(week2ExpandableList, position, week2ExpandableListAdaptor);

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
    }

    private void removeOrCopyProcedure(
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Select option")
                .setCancelable(false)
                .setPositiveButton("Remove", (dialog, removeButtonId) ->
                    removeProcedure(weekDay, procedure))
                .setNegativeButton("Copy", (dialog, copyButtonId) ->
                    copyProcedure(procedure))
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void saveClick(View view)
    {
        if (enabledCheckBox.isChecked() && !routine.isEnabled())
        {
            choreList.resetRoutine(routine);
        }

        routine.setName(nameEditText.getText().toString().trim());
        routine.setEnabled(enabledCheckBox.isChecked());
        routine.setStartDate(LocalDate.parse(startDateEditText.getText().toString()));

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

    @SuppressLint("SetTextI18n")
    public void addProcedureClick(View view)
    {
        ProcedureEditDialog.create(this, null, null, (time, description, week, weekDay) ->
        {
            Procedure procedure = new Procedure(description, time);

            routine.getWeek(week).getWeekDay(weekDay).addProcedure(procedure);

            week1ExpandableListAdaptor.notifyDataSetChanged();
            week2ExpandableListAdaptor.notifyDataSetChanged();
        });
    }

    private void removeProcedure(Routine.Day weekDay, Procedure procedure)
    {
        weekDay.removeProcedure(procedure);

        week1ExpandableListAdaptor.notifyDataSetChanged();
        week2ExpandableListAdaptor.notifyDataSetChanged();
    }

    private void copyProcedure(Procedure procedure)
    {
        ProcedureEditDialog.create(
            this,
            procedure.getTime(),
            procedure.getDescription(),
            (time, description, week, weekDay) ->
            {
                Procedure newProcedure = new Procedure(description, time);

                routine.getWeek(week).getWeekDay(weekDay).addProcedure(newProcedure);

                week1ExpandableListAdaptor.notifyDataSetChanged();
                week2ExpandableListAdaptor.notifyDataSetChanged();
            });
    }
}
