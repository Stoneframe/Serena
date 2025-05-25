package stoneframe.serena.gui.sleep;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private TextView sessionStartTimeTextView;
    private TextView sessionStopTimeTextView;
    private TextView sessionTotalTimeTextView;

    private TextView percentTextView;
    private Button toggleButton;

    private EditText startSessionEditText;
    private EditText endSessionEditText;
    private Button addSessionButton;

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

        sessionStartTimeTextView = rootView.findViewById(R.id.sessionStartTimeTextView);
        sessionStopTimeTextView = rootView.findViewById(R.id.sessionStopTimeTextView);
        sessionTotalTimeTextView = rootView.findViewById(R.id.sessionTotalTimeTextView);

        percentTextView = rootView.findViewById(R.id.percentTextView);
        toggleButton = rootView.findViewById(R.id.toggleButton);

        startSessionEditText = rootView.findViewById(R.id.startSessionEditText);
        endSessionEditText = rootView.findViewById(R.id.endSessionEditText);
        addSessionButton = rootView.findViewById(R.id.addSessionButton);

        toggleButton.setOnClickListener(v ->
        {
            sleepManager.toggle();

            updateComponents();
        });

        startSessionEditText.setOnClickListener(v -> showDatePicker(startSessionEditText));
        endSessionEditText.setOnClickListener(v -> showDatePicker(endSessionEditText));

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

        new ButtonEnabledLink(
            addSessionButton,
            new EditTextCriteria(startSessionEditText, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(endSessionEditText, EditTextCriteria.IS_NOT_EMPTY));

        updateComponents();

        return rootView;
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

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
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
        updatePreviousSession();
        updatePercentText();
        updatePercentTextColor();
        updateToggleButtonText();
        updateBedIcon();
    }

    private void updatePreviousSession()
    {
        Sleep.SleepSession session = sleepManager.getPreviousSession();

        if (session == null)
        {
            return;
        }

        sessionStartTimeTextView.setText(session.getStartTime().toString("yyyy-MM-dd HH:mm"));
        sessionStopTimeTextView.setText(session.getStopTime().toString("yyyy-MM-dd HH:mm"));
        sessionTotalTimeTextView.setText(formatMinutes(session.getSleepTime()));
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
        percentTextView.setText(sleepManager.getPercent() + " %");
    }

    private void updatePercentTextColor()
    {
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