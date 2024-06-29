package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;

public class ProcedureEditDialog
{
    public interface ProcedureEditListener
    {
        void onProcedureEdited(LocalTime time, String description);
    }

    public interface DayProcedureListener
    {
        void onProcedureCreated(LocalTime time, String description);
    }

    public interface WeekProcedureListener
    {
        void onProcedureCreated(LocalTime time, String description, int weekDay);
    }

    public interface FortnightProcedureListener
    {
        void onProcedureCreated(LocalTime time, String description, int week, int weekDay);
    }

    public static void edit(
        Context context,
        LocalTime time,
        String description,
        final ProcedureEditListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View dialogView = inflater.inflate(R.layout.dialog_procedure_edit, null);

        builder.setView(dialogView);

        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        final EditText descriptionText = dialogView.findViewById(R.id.editText);

        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        builder.setTitle("Routine Procedure");

        timePicker.setIs24HourView(true);
        timePicker.setHour(0);
        timePicker.setMinute(0);

        if (time != null)
        {
            timePicker.setHour(time.getHourOfDay());
            timePicker.setMinute(time.getMinuteOfHour());
        }

        if (description != null)
        {
            descriptionText.setText(description);
        }

        final AlertDialog alertDialog = builder.create();

        buttonSave.setOnClickListener(v ->
        {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String customText = descriptionText.getText().toString();

            listener.onProcedureEdited(new LocalTime(hour, minute), customText);

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new EditTextButtonEnabledLink(
            buttonSave,
            new EditTextCriteria(descriptionText, EditTextCriteria.IS_NOT_EMPTY));

        alertDialog.show();
    }

    public static void create(Context context, final DayProcedureListener listener)
    {
        edit(context, null, null, listener::onProcedureCreated);
    }

    public static void create(Context context, final WeekProcedureListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_week_procedure_edit, null);
        builder.setView(dialogView);

        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        final EditText editText = dialogView.findViewById(R.id.editText);
        final Spinner weekdaySpinner = dialogView.findViewById(R.id.weekdaySpinner);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        List<String> weekdays = new ArrayList<>(Arrays.asList(
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_spinner_item,
            weekdays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekdaySpinner.setAdapter(adapter);

        timePicker.setIs24HourView(true);
        timePicker.setHour(0);
        timePicker.setMinute(0);

        builder.setTitle("Add procedure");

        final AlertDialog alertDialog = builder.create();

        buttonSave.setOnClickListener(v ->
        {
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();
            String description = editText.getText().toString();
            int weekDayIndex = weekdaySpinner.getSelectedItemPosition() + 1;

            listener.onProcedureCreated(
                new LocalTime(hourOfDay, minute),
                description,
                weekDayIndex);

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new EditTextButtonEnabledLink(
            buttonSave,
            new EditTextCriteria(editText, EditTextCriteria.IS_NOT_EMPTY));

        alertDialog.show();
    }

    public static void create(Context context, final FortnightProcedureListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_fortnight_procedure_edit, null);
        builder.setView(dialogView);

        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        final EditText editText = dialogView.findViewById(R.id.editText);
        final Spinner weekdaySpinner = dialogView.findViewById(R.id.weekdaySpinner);
        final RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        final RadioButton radioButtonWeek1 = dialogView.findViewById(R.id.radioButtonWeek1);
        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        radioButtonWeek1.toggle();

        List<String> weekdays = new ArrayList<>(Arrays.asList(
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            context,
            android.R.layout.simple_spinner_item,
            weekdays);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        weekdaySpinner.setAdapter(adapter);

        timePicker.setIs24HourView(true);
        timePicker.setHour(0);
        timePicker.setMinute(0);

        builder.setTitle("Add procedure");

        final AlertDialog alertDialog = builder.create();

        buttonSave.setOnClickListener(v ->
        {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String description = editText.getText().toString();
            int week = getSelectedWeekNumber(radioGroup);
            int weekDay = weekdaySpinner.getSelectedItemPosition() + 1;

            listener.onProcedureCreated(new LocalTime(hour, minute), description, week, weekDay);

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new EditTextButtonEnabledLink(
            buttonSave,
            new EditTextCriteria(editText, EditTextCriteria.IS_NOT_EMPTY));

        alertDialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    private static int getSelectedWeekNumber(RadioGroup radioGroup)
    {
        switch (radioGroup.getCheckedRadioButtonId())
        {
            case R.id.radioButtonWeek1:
                return 1;
            case R.id.radioButtonWeek2:
                return 2;
            default:
                return -1;
        }
    }
}
