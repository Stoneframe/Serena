package stoneframe.chorelist.gui.chores;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.LocalDate;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.EditActivity;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.chores.ChoreEditor;

public class EditChoreActivity extends EditActivity implements ChoreEditor.ChoreEditorListener
{
    private DatePickerDialog nextPickerDialog;
    private LocalDate next;

    private CheckBox enabledCheckbox;
    private EditText nextEditText;
    private EditText descriptionEditText;
    private EditText priorityEditText;
    private EditText effortEditText;
    private Spinner intervalUnitSpinner;
    private EditText intervalLengthEditText;

    private ChoreEditor choreEditor;

    @Override
    public void isEnabledChanged()
    {

    }

    @Override
    public void nextChanged()
    {

    }

    @Override
    public void descriptionChanged()
    {

    }

    @Override
    public void priorityChanged()
    {

    }

    @Override
    public void effortChanged()
    {

    }

    @Override
    public void intervalUnitChanged()
    {

    }

    @Override
    public void intervalLengthChanged()
    {

    }

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_chore;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Chore";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Chore";
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void createActivity()
    {
        Chore chore = globalState.getActiveChore();

        choreEditor = choreList.getChoreManager().getChoreEditor(chore);

        next = choreEditor.getNext();

        nextPickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
        {
            next = new LocalDate(year, month + 1, dayOfMonth);
            nextEditText.setText(next.toString("yyyy-MM-dd"));
        }, next.getYear(), next.getMonthOfYear() - 1, next.getDayOfMonth());

        enabledCheckbox = findViewById(R.id.enableCheckbox);
        nextEditText = findViewById(R.id.nextEditText);
        descriptionEditText = findViewById(R.id.choreDescriptionEditText);
        priorityEditText = findViewById(R.id.priorityEditText);
        effortEditText = findViewById(R.id.effortEditText);
        intervalUnitSpinner = findViewById(R.id.intervalUnitSpinner);
        intervalLengthEditText = findViewById(R.id.intervalLengthEditText);
        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        intervalUnitSpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            new String[]{"Days", "Weeks", "Months", "Years"}));

        enabledCheckbox.setChecked(choreEditor.isEnabled());
        nextEditText.setText(next.toString("yyyy-MM-dd"));
        descriptionEditText.setText(choreEditor.getDescription());
        priorityEditText.setText(
            Integer.toString(choreEditor.getPriority()),
            TextView.BufferType.EDITABLE);
        effortEditText.setText(Integer.toString(choreEditor.getEffort()), TextView.BufferType.EDITABLE);
        intervalUnitSpinner.setSelection(choreEditor.getIntervalUnit());
        intervalLengthEditText.setText(
            Integer.toString(choreEditor.getIntervalLength()),
            TextView.BufferType.EDITABLE);

        nextEditText.setInputType(InputType.TYPE_NULL);
        nextEditText.setOnClickListener(view -> nextPickerDialog.show());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        choreEditor.addListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        choreEditor.removeListener(this);
    }

    @Override
    protected EditTextCriteria[] getSaveEnabledCriteria()
    {
        return new EditTextCriteria[]
            {
                new EditTextCriteria(descriptionEditText, EditTextCriteria.IS_NOT_EMPTY),
                new EditTextCriteria(priorityEditText, EditTextCriteria.IS_NOT_EMPTY),
                new EditTextCriteria(effortEditText, EditTextCriteria.IS_NOT_EMPTY),
                new EditTextCriteria(intervalLengthEditText, EditTextCriteria.IS_NOT_EMPTY),
            };
    }

    @Override
    protected void onCancel()
    {

    }

    @Override
    protected void onSave(int action)
    {
        boolean isEnabled = enabledCheckbox.isChecked();
        String description = descriptionEditText.getText().toString().trim();
        int priority = Integer.parseInt(priorityEditText.getText().toString());
        int effort = Integer.parseInt(effortEditText.getText().toString());
        int intervalUnit = (int)intervalUnitSpinner.getSelectedItemId();
        int intervalLength = Integer.parseInt(intervalLengthEditText.getText().toString());

        choreEditor.setEnabled(isEnabled);
        choreEditor.setDescription(description);
        choreEditor.setNext(next);
        choreEditor.setPriority(priority);
        choreEditor.setEffort(effort);
        choreEditor.setIntervalUnit(intervalUnit);
        choreEditor.setIntervalLength(intervalLength);

        choreEditor.save();

        choreList.save();
    }

    @Override
    protected void onRemove()
    {
        choreEditor.remove();
        choreList.save();
    }
}
