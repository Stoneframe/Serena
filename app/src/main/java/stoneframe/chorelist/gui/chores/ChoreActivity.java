package stoneframe.chorelist.gui.chores;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.LocalDate;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.chores.Chore;

public class ChoreActivity extends AppCompatActivity
{
    public static final int CHORE_ACTION_ADD = 0;
    public static final int CHORE_ACTION_EDIT = 1;

    private int action;

    private LocalDate next;

    private DatePickerDialog datePickerDialog;

    private CheckBox enabledCheckbox;
    private EditText nextEditText;
    private EditText descriptionEditText;
    private EditText priorityEditText;
    private EditText effortEditText;
    private Spinner intervalUnitSpinner;
    private EditText intervalLengthEditText;

    private Button cancelButton;
    private Button saveButton;

    private ChoreList choreList;

    private Chore chore;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem removeItem = menu.findItem(R.id.action_remove);

        if (removeItem != null)
        {
            removeItem.setVisible(action == CHORE_ACTION_EDIT);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_remove)
        {
            removeChore();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chore);

        setTitle("Chore");

        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();
        chore = globalState.getActiveChore();

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        next = chore.getNext();

        datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
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

        enabledCheckbox.setChecked(chore.isEnabled());
        nextEditText.setText(next.toString("yyyy-MM-dd"));
        descriptionEditText.setText(chore.getDescription());
        priorityEditText.setText(
            Integer.toString(chore.getPriority()),
            TextView.BufferType.EDITABLE);
        effortEditText.setText(Integer.toString(chore.getEffort()), TextView.BufferType.EDITABLE);
        intervalUnitSpinner.setSelection(chore.getIntervalUnit());
        intervalLengthEditText.setText(
            Integer.toString(chore.getIntervalLength()),
            TextView.BufferType.EDITABLE);

        nextEditText.setInputType(InputType.TYPE_NULL);
        nextEditText.setOnClickListener(view -> datePickerDialog.show());

        cancelButton.setOnClickListener(v -> cancelClick());
        saveButton.setOnClickListener(v -> saveClick());

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(descriptionEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(priorityEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(effortEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(intervalLengthEditText, EditTextCriteria.IS_NOT_EMPTY));
    }

    private void saveClick()
    {
        boolean isEnabled = enabledCheckbox.isChecked();
        String description = descriptionEditText.getText().toString().trim();
        int priority = Integer.parseInt(priorityEditText.getText().toString());
        int effort = Integer.parseInt(effortEditText.getText().toString());
        int intervalUnit = (int)intervalUnitSpinner.getSelectedItemId();
        int intervalLength = Integer.parseInt(intervalLengthEditText.getText().toString());

        chore.setEnabled(isEnabled);
        chore.setDescription(description);
        chore.setNext(next);
        chore.setPriority(priority);
        chore.setEffort(effort);
        chore.setIntervalUnit(intervalUnit);
        chore.setIntervalLength(intervalLength);

        if (action == CHORE_ACTION_ADD)
        {
            choreList.addChore(chore);
        }

        choreList.save();

        setResult(RESULT_OK);
        finish();
    }

    private void cancelClick()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void removeChore()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove Chore",
            "Are you sure you want to remove the chore?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                choreList.removeChore(chore);
                choreList.save();

                setResult(RESULT_OK);
                finish();
            });
    }
}
