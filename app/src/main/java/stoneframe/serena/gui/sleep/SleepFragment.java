package stoneframe.serena.gui.sleep;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Locale;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.MainActivity;
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

    private TextView percentTextView;
    private TextView rangeTextView;
    private TextView stateTextView;
    private Button toggleButton;
    private Button settingsButton;
    private Button addSessionButton;
    private Button previousSessionsButton;

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

        percentTextView = rootView.findViewById(R.id.percentTextView);
        rangeTextView = rootView.findViewById(R.id.rangeTextView);
        stateTextView = rootView.findViewById(R.id.stateTextView);
        toggleButton = rootView.findViewById(R.id.toggleButton);
        settingsButton = rootView.findViewById(R.id.settingsButton);
        addSessionButton = rootView.findViewById(R.id.addSessionButton);
        previousSessionsButton = rootView.findViewById(R.id.previousSessionsButton);

        toggleButton.setOnClickListener(v ->
        {
            sleepManager.toggle();

            updateComponents();
        });

        settingsButton.setOnClickListener(v -> showSettingsDialog());
        addSessionButton.setOnClickListener(v -> showAddSessionDialog());
        previousSessionsButton.setOnClickListener(v -> startActivity(
            new Intent(requireContext(), PreviousSleepSessionsActivity.class)));

        updateComponents();

        return rootView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (sleepManager != null)
        {
            updateComponents();
        }
    }

    private void showAddSessionDialog()
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_sleep_session, null);

        EditText startEditText = dialogView.findViewById(R.id.startEditText);
        EditText stopEditText = dialogView.findViewById(R.id.stopEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button removeButton = dialogView.findViewById(R.id.removeButton);
        Button addButton = dialogView.findViewById(R.id.saveButton);

        removeButton.setVisibility(View.GONE);
        addButton.setText("Add");
        startEditText.setOnClickListener(v -> showDatePicker(startEditText));
        stopEditText.setOnClickListener(v -> showDatePicker(stopEditText));

        new ButtonEnabledLink(
            addButton,
            new EditTextCriteria(startEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(stopEditText, EditTextCriteria.IS_NOT_EMPTY));

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Add sleep session")
            .setView(dialogView)
            .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        addButton.setOnClickListener(v ->
        {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
            LocalDateTime start = LocalDateTime.parse(
                startEditText.getText().toString(),
                formatter);
            LocalDateTime stop = LocalDateTime.parse(
                stopEditText.getText().toString(),
                formatter);

            try
            {
                sleepManager.addSession(start, stop);
            }
            catch (IllegalArgumentException e)
            {
                stopEditText.setError(e.getMessage());
                return;
            }

            serena.save();
            dialog.dismiss();
            updateComponents();
        });

        dialog.show();
    }

    private void showSettingsDialog()
    {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sleep_settings, null);

        CheckBox enableCheckbox = dialogView.findViewById(R.id.enableCheckbox);
        EditText minHoursEditText = dialogView.findViewById(R.id.minHoursSleepEditText);
        EditText minMinutesEditText = dialogView.findViewById(R.id.minMinutesSleepEditText);
        EditText maxHoursEditText = dialogView.findViewById(R.id.maxHoursSleepEditText);
        EditText maxMinutesEditText = dialogView.findViewById(R.id.maxMinutesSleepEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button okButton = dialogView.findViewById(R.id.okButton);

        enableCheckbox.setChecked(sleepManager.isEnabled());
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
                sleepManager.setEnabled(enableCheckbox.isChecked());
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
        updatePercentText();
        updatePercentTextColor();
        updateRangeText();
        updateToggleButtonText();
        updateBedIcon();
        updateComponentEnabled();
    }

    private void updateComponentEnabled()
    {
        boolean isEnabled = sleepManager.isEnabled();

        toggleButton.setEnabled(isEnabled);
        addSessionButton.setEnabled(isEnabled);
        previousSessionsButton.setEnabled(isEnabled);
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

    private void updateRangeText()
    {
        if (sleepManager.isEnabled())
        {
            rangeTextView.setText(String.format(
                Locale.getDefault(),
                "Target range: %s - %s",
                formatHours(sleepManager.getMinHoursSleepPerDay()),
                formatHours(sleepManager.getMaxHoursSleepPerDay())));
        }
        else
        {
            rangeTextView.setText("");
        }
    }

    private String formatHours(double hours)
    {
        int totalMinutes = (int)Math.round(hours * 60d);

        return String.format(
            Locale.getDefault(),
            "%dh %02dm",
            totalMinutes / 60,
            totalMinutes % 60);
    }

    private void updatePercentTextColor()
    {
        if (!sleepManager.isEnabled())
        {
            percentTextView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.text_disabled));
            return;
        }

        if (sleepManager.isAhead())
        {
            percentTextView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.status_success_on_dark));
        }
        else
        {
            percentTextView.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.status_error_on_dark));
        }
    }

    private void updateToggleButtonText()
    {
        if (sleepManager.getState() == Sleep.AWAKE)
        {
            stateTextView.setText("Awake");
            toggleButton.setText("Start sleep");
        }
        else
        {
            stateTextView.setText("Asleep");
            toggleButton.setText("Stop sleep");
        }
    }

    private void updateBedIcon()
    {
        MainActivity mainActivity = (MainActivity)requireActivity();

        mainActivity.updateSleepIconColor();
    }
}
