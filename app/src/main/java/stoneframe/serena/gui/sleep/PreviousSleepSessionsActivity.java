package stoneframe.serena.gui.sleep;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.sleep.Sleep;
import stoneframe.serena.sleep.SleepManager;

public class PreviousSleepSessionsActivity extends AppCompatActivity
{
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");

    private Serena serena;
    private SleepManager sleepManager;
    private List<Sleep.SleepSession> sessions;
    private SessionAdapter sessionAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        serena = GlobalState.getInstance().getSerena();
        sleepManager = serena.getSleepManager();

        setContentView(R.layout.activity_previous_sleep_sessions);
        setTitle("Previous sessions");

        refreshSessions();

        sessionAdapter = new SessionAdapter();

        ListView sessionListView = findViewById(R.id.sessionListView);
        TextView emptyTextView = findViewById(R.id.emptyTextView);
        Button doneButton = findViewById(R.id.doneButton);

        sessionListView.setEmptyView(emptyTextView);
        sessionListView.setAdapter(sessionAdapter);
        sessionListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Sleep.SleepSession session = (Sleep.SleepSession)sessionAdapter.getItem(position);
            showEditSessionDialog(session);
        });

        doneButton.setOnClickListener(v -> finish());
    }

    private void showEditSessionDialog(Sleep.SleepSession session)
    {
        android.view.View dialogView = getLayoutInflater().inflate(
            R.layout.dialog_edit_sleep_session,
            null);

        EditText startEditText = dialogView.findViewById(R.id.startEditText);
        EditText stopEditText = dialogView.findViewById(R.id.stopEditText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button removeButton = dialogView.findViewById(R.id.removeButton);
        Button saveButton = dialogView.findViewById(R.id.saveButton);

        startEditText.setText(formatDateTime(session.getStartTime()));
        stopEditText.setText(formatDateTime(session.getStopTime()));

        startEditText.setOnClickListener(v -> showDatePicker(startEditText));
        stopEditText.setOnClickListener(v -> showDatePicker(stopEditText));

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Edit sleep session")
            .setView(dialogView)
            .create();

        cancelButton.setOnClickListener(v -> dialog.dismiss());
        removeButton.setOnClickListener(v -> DialogUtils.showConfirmationDialog(
            this,
            "Remove sleep session",
            "Are you sure you want to remove this sleep session?",
            isConfirmed ->
            {
                if (!isConfirmed)
                {
                    return;
                }

                sleepManager.removeSession(session);
                serena.save();
                dialog.dismiss();
                refreshSessionList();
            }));
        saveButton.setOnClickListener(v ->
        {
            LocalDateTime start = LocalDateTime.parse(
                startEditText.getText().toString(),
                DATE_TIME_FORMATTER);
            LocalDateTime stop = LocalDateTime.parse(
                stopEditText.getText().toString(),
                DATE_TIME_FORMATTER);

            try
            {
                sleepManager.updateSession(session, start, stop);
            }
            catch (IllegalArgumentException e)
            {
                stopEditText.setError(e.getMessage());
                return;
            }

            serena.save();
            dialog.dismiss();
            refreshSessionList();
        });

        dialog.show();
    }

    private void showDatePicker(EditText editText)
    {
        LocalDateTime currentValue = LocalDateTime.parse(
            editText.getText().toString(),
            DATE_TIME_FORMATTER);

        new DatePickerDialog(
            this,
            (view, year, month, day) -> showTimePicker(
                editText,
                currentValue.withDate(year, month + 1, day)),
            currentValue.getYear(),
            currentValue.getMonthOfYear() - 1,
            currentValue.getDayOfMonth())
            .show();
    }

    private void showTimePicker(EditText editText, LocalDateTime dateTime)
    {
        new TimePickerDialog(
            this,
            (view, hour, minute) -> editText.setText(formatDateTime(
                dateTime.withTime(hour, minute, 0, 0))),
            dateTime.getHourOfDay(),
            dateTime.getMinuteOfHour(),
            true)
            .show();
    }

    private void refreshSessionList()
    {
        refreshSessions();
        sessionAdapter.notifyDataSetChanged();
    }

    private void refreshSessions()
    {
        sessions = sleepManager.getSessions();
        Collections.reverse(sessions);
    }

    private String formatDateTime(LocalDateTime dateTime)
    {
        return dateTime.toString(DATE_TIME_FORMATTER);
    }

    private String formatMinutes(Minutes minutes)
    {
        int totalMinutes = minutes.getMinutes();

        return String.format(
            Locale.getDefault(),
            "%02d:%02d",
            totalMinutes / 60,
            totalMinutes % 60);
    }

    private class SessionAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return sessions.size();
        }

        @Override
        public Sleep.SleepSession getItem(int position)
        {
            return sessions.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                convertView = LayoutInflater.from(PreviousSleepSessionsActivity.this)
                    .inflate(R.layout.list_item_sleep_session, parent, false);
            }

            Sleep.SleepSession session = getItem(position);
            TextView startTimeTextView = convertView.findViewById(R.id.startTimeTextView);
            TextView stopTimeTextView = convertView.findViewById(R.id.stopTimeTextView);
            TextView durationTextView = convertView.findViewById(R.id.durationTextView);

            startTimeTextView.setText(formatDateTime(session.getStartTime()));
            stopTimeTextView.setText(formatDateTime(session.getStopTime()));
            durationTextView.setText(formatMinutes(session.getSleepTime()));

            return convertView;
        }
    }
}
