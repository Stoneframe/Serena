package stoneframe.serena.gui.routines.fortnights;

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

import stoneframe.serena.R;
import stoneframe.serena.gui.util.ButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.model.routines.Procedure;

public class FortnightProcedureEditDialog
{
    public interface FortnightProcedureListener
    {
        void onProcedureComplete(Procedure procedure, int week, int weekDay);
    }

    public static void create(Context context, final FortnightProcedureListener listener)
    {
        showDialog(context, "Add Procedure", new LocalTime(0, 0), "", 1, 0, listener);
    }

    public static void copy(
        Context context,
        Procedure procedure,
        int week,
        int dayOfWeek,
        final FortnightProcedureListener listener)
    {
        showDialog(
            context,
            "Copy Procedure",
            procedure.getTime(),
            procedure.getDescription(),
            week,
            dayOfWeek,
            listener);
    }

    public static void edit(
        Context context,
        Procedure procedure,
        int week,
        int dayOfWeek,
        final FortnightProcedureListener listener)
    {
        showDialog(
            context,
            "Edit Procedure",
            procedure.getTime(),
            procedure.getDescription(),
            week,
            dayOfWeek,
            listener);
    }

    private static void showDialog(
        Context context,
        String dialogName,
        LocalTime initialTime,
        String initialDescription,
        int initialWeek,
        int initialDayOfWeek,
        FortnightProcedureListener listener)
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
        final RadioButton radioButtonWeek2 = dialogView.findViewById(R.id.radioButtonWeek2);

        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        editText.setText(initialDescription);

        if (initialWeek == 1)
        {
            radioButtonWeek1.toggle();
        }
        else
        {
            radioButtonWeek2.toggle();
        }

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
        weekdaySpinner.setSelection(initialDayOfWeek);

        timePicker.setIs24HourView(true);

        if (initialTime != null)
        {
            timePicker.setHour(initialTime.getHourOfDay());
            timePicker.setMinute(initialTime.getMinuteOfHour());
        }
        else
        {
            timePicker.setHour(0);
            timePicker.setMinute(0);
        }

        builder.setTitle(dialogName);

        final AlertDialog alertDialog = builder.create();

        buttonSave.setOnClickListener(v ->
        {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String description = editText.getText().toString().trim();
            int week = getSelectedWeekNumber(radioGroup);
            int weekDay = weekdaySpinner.getSelectedItemPosition() + 1;

            listener.onProcedureComplete(
                new Procedure(description, new LocalTime(hour, minute)),
                week,
                weekDay);

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new ButtonEnabledLink(
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
