package stoneframe.serena.gui.sleep;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Locale;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.MainActivity;
import stoneframe.serena.gui.util.enable.BooleanCriteria;
import stoneframe.serena.gui.util.enable.ButtonEnabledLink;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.sleep.Sleep;
import stoneframe.serena.sleep.SleepManager;

@SuppressLint("SetTextI18n")
public class SleepFragment extends Fragment
{
    private GlobalState globalState;
    private Serena serena;
    private SleepManager sleepManager;

    private CheckBox enableCheckbox;

    private TextView sessionStartTimeTextView;
    private TextView sessionStopTimeTextView;
    private TextView sessionTotalTimeTextView;

    private TextView percentTextView;
    private Button toggleButton;
    private Button settingsButton;

    private EditText startSessionEditText;
    private EditText endSessionEditText;
    private Button addSessionButton;
    private ButtonEnabledLink addSessionButtonEnableLink;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();
        sleepManager = serena.getSleepManager();

        View rootView = inflater.inflate(R.layout.fragment_sleep, container, false);

        enableCheckbox = rootView.findViewById(R.id.enableCheckbox);

        sessionStartTimeTextView = rootView.findViewById(R.id.sessionStartTimeTextView);
        sessionStopTimeTextView = rootView.findViewById(R.id.sessionStopTimeTextView);
        sessionTotalTimeTextView = rootView.findViewById(R.id.sessionTotalTimeTextView);

        percentTextView = rootView.findViewById(R.id.percentTextView);
        toggleButton = rootView.findViewById(R.id.toggleButton);
        settingsButton = rootView.findViewById(R.id.settingsButton);

        startSessionEditText = rootView.findViewById(R.id.startSessionEditText);
        endSessionEditText = rootView.findViewById(R.id.endSessionEditText);
        addSessionButton = rootView.findViewById(R.id.addSessionButton);

        enableCheckbox.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            sleepManager.setEnabled(isChecked);

            updateComponents();
        });

        toggleButton.setOnClickListener(v ->
        {
            sleepManager.toggle();

            updateComponents();
        });

        settingsButton.setOnClickListener(v -> showSettingsDialog());

        startSessionEditText.setOnClickListener(v -> showDatePicker(startSessionEditText));
        endSessionEditText.setOnClickListener(v -> showDatePicker(endSessionEditText));

        addSessionButton.setEnabled(false);
        addSessionButton.setOnClickListener(v ->
        {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

            LocalDateTime start = LocalDateTime.parse(
                startSessionEditText.getText().toString(),
                formatter);
            LocalDateTime end = LocalDateTime.parse(
                endSessionEditText.getText().toString(),
                formatter);

            sleepManager.addSession(start, end);

            startSessionEditText.getText().clear();
            endSessionEditText.getText().clear();

            updateComponents();
        });

        addSessionButtonEnableLink = new ButtonEnabledLink(
            addSessionButton,
            new EditTextCriteria(startSessionEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(endSessionEditText, EditTextCriteria.IS_NOT_EMPTY),
            new BooleanCriteria(sleepManager::isEnabled));

        updateComponents();

        return rootView;
    }

    private void showSettingsDialog()
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sleep_settings, null);

        EditText minHoursEditText = dialogView.findViewById(R.id.minHoursSleepEditText);
        EditText minMinutesEditText = dialogView.findViewById(R.id.minMinutesSleepEditText);
        EditText maxHoursEditText = dialogView.findViewById(R.id.maxHoursSleepEditText);
        EditText maxMinutesEditText = dialogView.findViewById(R.id.maxMinutesSleepEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button okButton = dialogView.findViewById(R.id.okButton);

        setTimeSpan(minHoursEditText, minMinutesEditText, sleepManager.getMinHoursSleepPerDay());
        setTimeSpan(maxHoursEditText, maxMinutesEditText, sleepManager.getMaxHoursSleepPerDay());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        okButton.setOnClickListener(v ->
        {
            Double minHours = getTimeSpanHours(minHoursEditText, minMinutesEditText);
            Double maxHours = getTimeSpanHours(maxHoursEditText, maxMinutesEditText);

            if (minHours == null)
            {
                setTimeSpanError(minHoursEditText, minMinutesEditText);
                return;
            }

            if (maxHours == null)
            {
                setTimeSpanError(maxHoursEditText, maxMinutesEditText);
                return;
            }

            try
            {
                sleepManager.setSleepRange(minHours, maxHours);
            }
            catch (IllegalArgumentException e)
            {
                maxHoursEditText.setError(e.getMessage());
                return;
            }

            dialog.dismiss();
            updateComponents();
        });

        dialog.show();
    }

    @Nullable
    private Double getTimeSpanHours(EditText hoursEditText, EditText minutesEditText)
    {
        Integer hours = getInteger(hoursEditText);
        Integer minutes = getInteger(minutesEditText);

        if (hours == null || minutes == null || minutes < 0 || minutes > 59)
        {
            return null;
        }

        return hours + minutes / 60d;
    }

    @Nullable
    private Integer getInteger(EditText editText)
    {
        try
        {
            String value = editText.getText().toString().trim();

            return value.isEmpty()
                ? null
                : Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private void setTimeSpan(EditText hoursEditText, EditText minutesEditText, double hours)
    {
        int totalMinutes = (int)Math.round(hours * 60d);

        hoursEditText.setText(String.format(Locale.getDefault(), "%d", totalMinutes / 60));
        minutesEditText.setText(String.format(Locale.getDefault(), "%02d", totalMinutes % 60));
    }

    private void setTimeSpanError(EditText hoursEditText, EditText minutesEditText)
    {
        hoursEditText.setError("Enter hours");
        minutesEditText.setError("Enter minutes 0-59");
    }

    private void showDatePicker(EditText dateEditText)
    {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            requireContext(),
            (view, selectedYear, selectedMonth, selectedDay) ->
            {
                calendar.set(Calendar.YEAR, selectedYear);
                calendar.set(Calendar.MONTH, selectedMonth);
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);

                showTimePicker(dateEditText, calendar);
            }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker(EditText dateTimeEditText, Calendar calendar)
    {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
            requireContext(),
            (view, selectedHour, selectedMinute) ->
            {
                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                calendar.set(Calendar.MINUTE, selectedMinute);

                String dateTime = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d %02d:%02d",
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1,
                    calendar.get(Calendar.DAY_OF_MONTH),
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE));

                dateTimeEditText.setText(dateTime);
            }, hour, minute, true);

        timePickerDialog.show();
    }

    private void updateComponents()
    {
        updateIsEnabled();
        updatePreviousSession();
        updatePercentText();
        updatePercentTextColor();
        updateToggleButtonText();
        updateBedIcon();
        updateComponentEnabled();
    }

    private void updateIsEnabled()
    {
        enableCheckbox.setChecked(sleepManager.isEnabled());
    }

    private void updateComponentEnabled()
    {
        boolean isEnabled = sleepManager.isEnabled();

        toggleButton.setEnabled(isEnabled);
        startSessionEditText.setEnabled(isEnabled);
        endSessionEditText.setEnabled(isEnabled);
        addSessionButtonEnableLink.criteriaValueChanged();
    }

    private void updatePreviousSession()
    {
        Sleep.SleepSession session = sleepManager.getPreviousSession();

        if (session == null)
        {
            return;
        }

        if (sleepManager.isEnabled())
        {
            sessionStartTimeTextView.setText(session.getStartTime().toString("yyyy-MM-dd HH:mm"));
            sessionStopTimeTextView.setText(session.getStopTime().toString("yyyy-MM-dd HH:mm"));
            sessionTotalTimeTextView.setText(formatMinutes(session.getSleepTime()));
        }
        else
        {
            sessionStartTimeTextView.setText("");
            sessionStopTimeTextView.setText("");
            sessionTotalTimeTextView.setText("");
        }

    }

    private String formatMinutes(Minutes minutes)
    {
        int totalMinutes = minutes.getMinutes();
        int hours = totalMinutes / 60;
        int remainingMinutes = totalMinutes % 60;

        return String.format(Locale.getDefault(), "%02d:%02d", hours, remainingMinutes);
    }

    private void updatePercentText()
    {
        if (sleepManager.isEnabled())
        {
            percentTextView.setText(sleepManager.getPercent() + " %");
        }
        else
        {
            percentTextView.setText("-");
        }
    }

    private void updatePercentTextColor()
    {
        if (!sleepManager.isEnabled())
        {
            percentTextView.setTextColor(Color.GRAY);
            return;
        }

        if (sleepManager.isAhead())
        {
            percentTextView.setTextColor(Color.GREEN);
        }
        else
        {
            percentTextView.setTextColor(Color.RED);
        }
    }

    private void updateToggleButtonText()
    {
        if (sleepManager.getState() == Sleep.AWAKE)
        {
            toggleButton.setText("AWAKE");
        }
        else
        {
            toggleButton.setText("ASLEEP");
        }
    }

    private void updateBedIcon()
    {
        MainActivity mainActivity = (MainActivity)requireActivity();

        mainActivity.updateSleepIconColor();
    }
}
