package stoneframe.serena.gui.tasks;

import android.app.DatePickerDialog;
import android.text.InputType;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import org.joda.time.LocalDate;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.SpeechRecognizerUtil;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.tasks.TaskEditor;

public class EditTaskActivity extends EditActivity
{
    private LocalDate deadline;
    private LocalDate ignoreBefore;

    private EditText descriptionEditText;
    private EditText deadlineEditText;
    private EditText ignoreBeforeEditText;
    private CheckBox isDoneCheckBox;

    private ImageButton speakButton;

    private TaskEditor taskEditor;

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_task;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Task";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Task";
    }

    @Override
    protected void createActivity()
    {
        Task task = globalState.getActiveTask();

        taskEditor = serena.getTaskManager().getTaskEditor(task);

        deadline = taskEditor.getDeadline();
        ignoreBefore = taskEditor.getIgnoreBefore();

        descriptionEditText = findViewById(R.id.taskDescriptionEditText);
        deadlineEditText = findViewById(R.id.deadlineEditText);
        ignoreBeforeEditText = findViewById(R.id.ignoreBeforeEditText);
        isDoneCheckBox = findViewById(R.id.isDoneCheckBox);
        speakButton = findViewById(R.id.speakButton);

        descriptionEditText.setText(taskEditor.getDescription());
        deadlineEditText.setText(deadline.toString("yyyy-MM-dd"));
        ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
        isDoneCheckBox.setChecked(taskEditor.isDone());

        deadlineEditText.setInputType(InputType.TYPE_NULL);
        deadlineEditText.setOnClickListener(view -> showDeadlineDatePickerDialog());

        ignoreBeforeEditText.setInputType(InputType.TYPE_NULL);
        ignoreBeforeEditText.setOnClickListener(view -> showIgnoreBeforeDatePickerDialog());

        SpeechRecognizerUtil.setup(this, speakButton, (text, hint) ->
        {
            descriptionEditText.setText(text);
            descriptionEditText.setHint(hint);
        });
    }

    @Override
    protected void startActivity()
    {
    }

    @Override
    protected void stopActivity()
    {
    }

    @Override
    protected EnableCriteria[] getSaveEnabledCriteria()
    {
        return new EditTextCriteria[]
            {
                new EditTextCriteria(descriptionEditText, EditTextCriteria.IS_NOT_EMPTY),
            };
    }

    @Override
    protected boolean onCancel()
    {
        return true;
    }

    @Override
    protected boolean onSave(int action)
    {
        String description = descriptionEditText.getText().toString().trim();
        boolean isDone = isDoneCheckBox.isChecked();

        taskEditor.setDescription(description);
        taskEditor.setDeadline(deadline);
        taskEditor.setIgnoreBefore(ignoreBefore);
        taskEditor.setDone(isDone);

        taskEditor.save();

        return true;
    }

    @Override
    protected void onRemove()
    {
        taskEditor.remove();
    }

    private void showDeadlineDatePickerDialog()
    {
        DatePickerDialog deadlinePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) ->
            {
                deadline = new LocalDate(year, month + 1, dayOfMonth);
                deadlineEditText.setText(deadline.toString("yyyy-MM-dd"));

                if (ignoreBefore.isAfter(deadline))
                {
                    ignoreBefore = deadline;
                    ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
                }
            },
            deadline.getYear(),
            deadline.getMonthOfYear() - 1,
            deadline.getDayOfMonth());

        DatePicker datePicker = deadlinePickerDialog.getDatePicker();
        datePicker.setMinDate(LocalDate.now().toDateTimeAtStartOfDay().getMillis());

        deadlinePickerDialog.show();
    }

    private void showIgnoreBeforeDatePickerDialog()
    {
        DatePickerDialog ignoreBeforePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) ->
            {
                ignoreBefore = new LocalDate(year, month + 1, dayOfMonth);
                ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
            },
            ignoreBefore.getYear(),
            ignoreBefore.getMonthOfYear() - 1,
            ignoreBefore.getDayOfMonth());

        DatePicker datePicker = ignoreBeforePickerDialog.getDatePicker();
        datePicker.setMinDate(LocalDate.now().toDateTimeAtStartOfDay().getMillis());
        datePicker.setMaxDate(deadline.toDateTimeAtStartOfDay().getMillis());

        ignoreBeforePickerDialog.show();
    }
}
