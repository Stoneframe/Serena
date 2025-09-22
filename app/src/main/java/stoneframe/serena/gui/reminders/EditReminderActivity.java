package stoneframe.serena.gui.reminders;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.text.InputType;
import android.widget.DatePicker;
import android.widget.EditText;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Calendar;
import java.util.Locale;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.reminders.ReminderEditor;

public class EditReminderActivity extends EditActivity
{
    private LocalDateTime dateTime;
    private String text;

    private EditText dateTimeEditText;
    private EditText textEditText;

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

        dateTime = reminderEditor.getDateTime();
        text = reminderEditor.getText();

        dateTimeEditText = findViewById(R.id.dateTimeEditText);
        textEditText = findViewById(R.id.textEditText);

        dateTimeEditText.setText(reminderEditor.getDateTime().toString("yyyy-MM-dd HH:mm"));
        textEditText.setText(reminderEditor.getText());

        dateTimeEditText.setInputType(InputType.TYPE_NULL);
        dateTimeEditText.setOnClickListener(v -> showDatePicker(dateTimeEditText));
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

    private void showDatePicker(EditText dateEditText)
    {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
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
            this,
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
}