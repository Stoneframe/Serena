package stoneframe.chorelist.gui.tasks;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.joda.time.LocalDate;

import java.util.List;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.tasks.Task;

public class TaskActivity extends AppCompatActivity
{
    public static final int TASK_ACTION_ADD = 0;
    public static final int TASK_ACTION_EDIT = 1;

    private int action;

    private LocalDate deadline;
    private LocalDate ignoreBefore;

    private EditText descriptionEditText;
    private EditText deadlineEditText;
    private EditText ignoreBeforeEditText;
    private CheckBox isDoneCheckBox;

    private Button cancelButton;
    private Button saveButton;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private ImageButton speakButton;

    private ChoreList choreList;

    private Task task;

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
            removeItem.setVisible(action == TASK_ACTION_EDIT);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_remove)
        {
            removeTask();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        setTitle("Task");

        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();
        task = globalState.getActiveTask();

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        deadline = task.getDeadline();
        ignoreBefore = task.getIgnoreBefore();

        descriptionEditText = findViewById(R.id.taskDescriptionEditText);
        deadlineEditText = findViewById(R.id.deadlineEditText);
        ignoreBeforeEditText = findViewById(R.id.ignoreBeforeEditText);
        isDoneCheckBox = findViewById(R.id.isDoneCheckBox);

        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        descriptionEditText.setText(task.getDescription());
        deadlineEditText.setText(deadline.toString("yyyy-MM-dd"));
        ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
        isDoneCheckBox.setChecked(task.isDone());

        deadlineEditText.setInputType(InputType.TYPE_NULL);
        deadlineEditText.setOnClickListener(view -> showDeadlineDatePickerDialog());

        ignoreBeforeEditText.setInputType(InputType.TYPE_NULL);
        ignoreBeforeEditText.setOnClickListener(view -> showIgnoreBeforeDatePickerDialog());

        cancelButton.setOnClickListener(v -> cancelClick());
        saveButton.setOnClickListener(v -> saveClick());

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(descriptionEditText, EditTextCriteria.IS_NOT_EMPTY));

        setupSpeechRecognizer();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        speechRecognizer.destroy();
    }

    private void saveClick()
    {
        String description = descriptionEditText.getText().toString().trim();
        boolean isDone = isDoneCheckBox.isChecked();

        task.setDescription(description);
        task.setDeadline(deadline);
        task.setIgnoreBefore(ignoreBefore);

        if (isDone != task.isDone())
        {
            if (isDone)
            {
                choreList.taskDone(task);
            }
            else
            {
                choreList.taskUndone(task);
            }
        }

        if (action == TASK_ACTION_ADD)
        {
            choreList.addTask(task);
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

    private void removeTask()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove Task",
            "Are you sure you want to remove the task?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                choreList.removeTask(task);
                choreList.save();

                setResult(RESULT_OK);
                finish();
            });
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

    private void setupSpeechRecognizer()
    {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sv-SE");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speakButton = findViewById(R.id.speakButton);
        speakButton.setOnClickListener(v ->
        {
            if (hasMicrophonePermission())
            {
                requestMicrophonePermission();
            }
            else
            {
                speechRecognizer.startListening(speechRecognizerIntent);
            }

        });

        speechRecognizer.setRecognitionListener(new RecognitionListener()
        {
            @Override
            public void onReadyForSpeech(Bundle params)
            {
                descriptionEditText.setText("");
                descriptionEditText.setHint("Ready...");
            }

            @Override
            public void onBeginningOfSpeech()
            {
                descriptionEditText.setText("");
                descriptionEditText.setHint("Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB)
            {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech()
            {
                descriptionEditText.setText("");
                descriptionEditText.setHint("Processing...");
            }

            @Override
            public void onError(int error)
            {
                String message = getErrorMessage(error);

                descriptionEditText.setHint("");

                Toast.makeText(TaskActivity.this, error + " Error: " + message, Toast.LENGTH_SHORT)
                    .show();
            }

            private @NonNull String getErrorMessage(int error)
            {
                switch (error)
                {
                    case SpeechRecognizer.ERROR_AUDIO:
                        return "Audio recording error";
                    case SpeechRecognizer.ERROR_CLIENT:
                        return "Client side error";
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        return "Insufficient permissions";
                    case SpeechRecognizer.ERROR_NETWORK:
                        return "Network error";
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        return "Network timeout";
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        return "No match";
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        return "Recognizer busy";
                    case SpeechRecognizer.ERROR_SERVER:
                        return "Server error";
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        return "No speech input";
                    case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
                        return "Language not supported";
                    default:
                        return "Unknown error";
                }
            }

            @Override
            public void onResults(Bundle results)
            {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null)
                {
                    String text = matches.get(0);

                    descriptionEditText.setText(capitalizeFirstLetter(text));
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });
    }

    private boolean hasMicrophonePermission()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED;
    }

    private void requestMicrophonePermission()
    {
        ActivityCompat.requestPermissions(
            this,
            new String[]{Manifest.permission.RECORD_AUDIO},
            1);
    }

    private String capitalizeFirstLetter(String text)
    {
        if (text == null || text.isEmpty())
        {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
