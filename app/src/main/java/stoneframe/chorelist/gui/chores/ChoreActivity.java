package stoneframe.chorelist.gui.chores;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.LocalDate;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;

public class ChoreActivity extends AppCompatActivity
{
    public static final int CHORE_ACTION_ADD = 0;
    public static final int CHORE_ACTION_EDIT = 1;

    public static final int CHORE_RESULT_SAVE = 0;
    public static final int CHORE_RESULT_REMOVE = 1;

    private int action;

    private boolean isEnabled;
    private LocalDate next;
    private String description;
    private int priority;
    private int effort;
    private int intervalUnit;
    private int intervalLength;

    private DatePickerDialog datePickerDialog;

    private CheckBox enabledCheckbox;
    private EditText nextEditText;
    private EditText descriptionEditText;
    private EditText priorityEditText;
    private EditText effortEditText;
    private Spinner intervalUnitSpinner;
    private EditText intervalLengthEditText;

    private Button saveButton;

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
            DialogUtils.showConfirmationDialog(
                this,
                "Remove Chore",
                "Are you sure you want to remove the chore?",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    Intent intent = new Intent();

                    intent.putExtra("RESULT", CHORE_RESULT_REMOVE);

                    setResult(RESULT_OK, intent);
                    finish();
                });

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

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        isEnabled = intent.getBooleanExtra("IsEnabled", false);
        next = (LocalDate)intent.getSerializableExtra("Next");
        description = intent.getStringExtra("Description");
        priority = intent.getIntExtra("Priority", 1);
        effort = intent.getIntExtra("Effort", 1);
        intervalUnit = intent.getIntExtra("IntervalUnit", 0);
        intervalLength = intent.getIntExtra("IntervalLength", 1);

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
        saveButton = findViewById(R.id.saveButton);

        intervalUnitSpinner.setAdapter(new ArrayAdapter<>(
            this,
            android.R.layout.simple_list_item_1,
            new String[]{"Days", "Weeks", "Months", "Years"}));

        enabledCheckbox.setChecked(isEnabled);
        nextEditText.setText(next.toString("yyyy-MM-dd"));
        descriptionEditText.setText(description);
        priorityEditText.setText(Integer.toString(priority), TextView.BufferType.EDITABLE);
        effortEditText.setText(Integer.toString(effort), TextView.BufferType.EDITABLE);
        intervalUnitSpinner.setSelection(intervalUnit);
        intervalLengthEditText.setText(
            Integer.toString(intervalLength),
            TextView.BufferType.EDITABLE);

        nextEditText.setInputType(InputType.TYPE_NULL);
        nextEditText.setOnClickListener(view -> datePickerDialog.show());

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(descriptionEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(priorityEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(effortEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(intervalLengthEditText, EditTextCriteria.IS_NOT_EMPTY));
    }

    public void saveClick(View view)
    {
        isEnabled = enabledCheckbox.isChecked();
        description = descriptionEditText.getText().toString().trim();
        priority = Integer.parseInt(priorityEditText.getText().toString());
        effort = Integer.parseInt(effortEditText.getText().toString());
        intervalUnit = (int)intervalUnitSpinner.getSelectedItemId();
        intervalLength = Integer.parseInt(intervalLengthEditText.getText().toString());

        Intent intent = new Intent();

        intent.putExtra("RESULT", CHORE_RESULT_SAVE);
        intent.putExtra("ACTION", action);
        intent.putExtra("IsEnabled", isEnabled);
        intent.putExtra("Next", next);
        intent.putExtra("Description", description);
        intent.putExtra("Priority", priority);
        intent.putExtra("Effort", effort);
        intent.putExtra("IntervalUnit", intervalUnit);
        intent.putExtra("IntervalLength", intervalLength);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelClick(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }
}
