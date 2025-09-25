package stoneframe.serena.gui.tasks;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.joda.time.LocalDate;

import java.util.List;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
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

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
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

        descriptionEditText.setText(taskEditor.getDescription());
        deadlineEditText.setText(deadline.toString("yyyy-MM-dd"));
        ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
        isDoneCheckBox.setChecked(taskEditor.isDone());

        deadlineEditText.setInputType(InputType.TYPE_NULL);
        deadlineEditText.setOnClickListener(view -> showDeadlineDatePickerDialog());

        ignoreBeforeEditText.setInputType(InputType.TYPE_NULL);
        ignoreBeforeEditText.setOnClickListener(view -> showIgnoreBeforeDatePickerDialog());

        setupSpeechRecognizer();
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
        speakButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!SpeechRecognizer.isRecognitionAvailable(EditTaskActivity.this))
                {
                    Toast.makeText(
                        EditTaskActivity.this,
                        "Speech recognition is not available on this device",
                        Toast.LENGTH_LONG).show();
                }
                else if (!hasMicrophonePermission())
                {
                    requestMicrophonePermission();
                }
                else
                {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            private boolean hasMicrophonePermission()
            {
                return ContextCompat.checkSelfPermission(EditTaskActivity.this, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            }

            private void requestMicrophonePermission()
            {
                ActivityCompat.requestPermissions(
                    EditTaskActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
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
                descriptionEditText.setHint("");

                String text = error + " Error: " + getErrorMessage(error);

                Toast.makeText(EditTaskActivity.this, text, Toast.LENGTH_SHORT).show();
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

    private String capitalizeFirstLetter(String text)
    {
        if (text == null || text.isEmpty())
        {
            return text;
        }

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }
}
