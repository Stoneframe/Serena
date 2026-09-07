package stoneframe.serena.gui.tasks;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.enable.ButtonEnabledLink;
import stoneframe.serena.gui.util.enable.CheckboxCriteria;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.OrEnableCriteria;
import stoneframe.serena.tasks.TaskManager;

public class TaskSettingsActivity extends AppCompatActivity
{
    private CheckBox limitTasksCheckBox;
    private EditText maximumTasksEditText;
    private TextView maximumTasksTextView;

    private Button cancelButton;
    private Button saveButton;

    private Serena serena;
    private TaskManager taskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_settings);

        serena = GlobalState.getInstance().getSerena();
        taskManager = serena.getTaskManager();

        limitTasksCheckBox = findViewById(R.id.limitTasksCheckBox);
        maximumTasksEditText = findViewById(R.id.maximumTasksEditText);
        maximumTasksTextView = findViewById(R.id.maximumTasksTextView);

        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        Integer maximumNumberOfTasks = taskManager.getMaximumNumberOfTasksPerDay();
        boolean isLimited = maximumNumberOfTasks != null;

        limitTasksCheckBox.setChecked(isLimited);

        if (isLimited)
        {
            maximumTasksEditText.setText(Integer.toString(maximumNumberOfTasks));
        }

        updateMaximumTasksControls();

        limitTasksCheckBox.setOnClickListener(v -> updateMaximumTasksControls());

        cancelButton.setOnClickListener(v -> onCancelClick());
        saveButton.setOnClickListener(v -> onSaveClick());

        new ButtonEnabledLink(
            saveButton,
            new OrEnableCriteria(
                new CheckboxCriteria(limitTasksCheckBox, CheckboxCriteria.IS_NOT_CHECKED),
                new EditTextCriteria(maximumTasksEditText, TaskSettingsActivity::isValidMaximumNumberOfTasks)));
    }

    private void onSaveClick()
    {
        Integer maximumNumberOfTasks = limitTasksCheckBox.isChecked()
            ? parseMaximumNumberOfTasks()
            : null;

        taskManager.setMaximumNumberOfTasksPerDay(maximumNumberOfTasks);
        serena.save();

        setResult(RESULT_OK);
        finish();
    }

    private void onCancelClick()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    private Integer parseMaximumNumberOfTasks()
    {
        try
        {
            int maximumNumberOfTasks = Integer.parseInt(maximumTasksEditText.getText().toString());

            return maximumNumberOfTasks >= 1 ? maximumNumberOfTasks : null;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static boolean isValidMaximumNumberOfTasks(EditText editText)
    {
        try
        {
            return Integer.parseInt(editText.getText().toString()) >= 1;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    private void updateMaximumTasksControls()
    {
        boolean isLimited = limitTasksCheckBox.isChecked();

        maximumTasksEditText.setEnabled(isLimited);

        if (isLimited)
        {
            maximumTasksTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
        else
        {
            maximumTasksTextView.setTextColor(ContextCompat.getColor(this, R.color.text_disabled));
            maximumTasksEditText.setText("");
        }
    }
}
