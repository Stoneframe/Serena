package stoneframe.serena.gui.reminders;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.reminders.ReminderEditor;

public class EditReminderActivity extends EditActivity
{
    private EditText dateTimeEditText;
    private EditText textEditText;
    private ImageButton speakButton;

    private ReminderEditor reminderEditor;

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_edit_reminder;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Reminder";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Reminder";
    }

    @Override
    protected void createActivity()
    {
        Reminder reminder = globalState.getActiveReminder();

        reminderEditor = serena.getReminderManager().getEditor(reminder);

        dateTimeEditText = findViewById(R.id.dateTimeEditText);
        textEditText = findViewById(R.id.textEditText);
        speakButton = findViewById(R.id.speakButton);

        dateTimeEditText.setText(reminderEditor.getDateTime().toString("yyyy-MM-dd HH:mm"));
        textEditText.setText(reminderEditor.getText());

        dateTimeEditText.setInputType(InputType.TYPE_NULL);
        dateTimeEditText.setOnClickListener(v -> showDatePicker());

        setupSpeechRecognizer(this, speakButton, (text, hint) ->
        {
            textEditText.setText(text);
            textEditText.setHint(hint);
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
                new EditTextCriteria(textEditText, EditTextCriteria.IS_NOT_EMPTY),
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
        LocalDateTime dateTime = LocalDateTime.parse(
            dateTimeEditText.getText().toString(),
            DateTimeFormat.forPattern("yyyy-MM-dd HH:mm"));
        String text = textEditText.getText().toString().trim();

        reminderEditor.setDateTime(dateTime);
        reminderEditor.setText(text);

        reminderEditor.save();

        return true;
    }

    @Override
    protected void onRemove()
    {
        reminderEditor.remove();
    }

    private void showDatePicker()
    {
        final Calendar calendar = Calendar.getInstance();

        final LocalDateTime dateTime = reminderEditor.getDateTime();

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, selectedYear, selectedMonth, selectedDay) ->
            {
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                showTimePicker(calendar);
            }, dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());

        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar)
    {
        LocalDateTime dateTime = reminderEditor.getDateTime();

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            (view, selectedHour, selectedMinute) ->
            {
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                String dateTimeStr = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d %02d:%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));

                dateTimeEditText.setText(dateTimeStr);
            }, dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), true);

        timePickerDialog.show();
    }

    private static void setupSpeechRecognizer(
        Activity activity,
        ImageButton speakButton,
        BiConsumer<String, String> callback)
    {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sv-SE");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speakButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!SpeechRecognizer.isRecognitionAvailable(activity))
                {
                    Toast.makeText(
                        activity,
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
                return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            }

            private void requestMicrophonePermission()
            {
                ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener()
        {
            @Override
            public void onReadyForSpeech(Bundle params)
            {
                callback.accept("", "Ready...");
            }

            @Override
            public void onBeginningOfSpeech()
            {
                callback.accept("", "Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB)
            {
            }

            @Override
            public void onBufferReceived(byte[] buffer)
            {
            }

            @Override
            public void onEndOfSpeech()
            {
                callback.accept("", "Processing...");
            }

            @Override
            public void onError(int error)
            {
                callback.accept("", "");

                String text = error + " Error: " + getErrorMessage(error);

                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results)
            {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null)
                {
                    String text = matches.get(0);

                    callback.accept(capitalizeFirstLetter(text), "");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults)
            {
            }

            @Override
            public void onEvent(int eventType, Bundle params)
            {
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

            private String capitalizeFirstLetter(String text)
            {
                if (text == null || text.isEmpty())
                {
                    return text;
                }

                return text.substring(0, 1).toUpperCase() + text.substring(1);
            }
        });
    }
}