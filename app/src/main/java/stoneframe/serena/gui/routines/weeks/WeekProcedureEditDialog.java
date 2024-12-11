package stoneframe.serena.gui.routines.weeks;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stoneframe.serena.R;
import stoneframe.serena.gui.util.EditTextButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.model.routines.Procedure;

public class WeekProcedureEditDialog
{
    public interface WeekProcedureListener
    {
        void onProcedureComplete(Procedure procedure, int weekDay);
    }

    public static void create(Context context, final WeekProcedureListener listener)
    {
        showDialog(context, "Add procedure", new LocalTime(0, 0), "", 0, listener);
    }

    public static void copy(
        Context context,
        Procedure procedure,
        int dayOfWeek,
        final WeekProcedureListener listener)
    {
        showDialog(
            context,
            "Copy procedure",
            procedure.getTime(),
            procedure.getDescription(),
            dayOfWeek,
            listener);
    }

    public static void edit(
        Context context,
        Procedure procedure,
        int dayOfWeek,
        final WeekProcedureListener listener)
    {
        showDialog(
            context,
            "Edit procedure",
            procedure.getTime(),
            procedure.getDescription(),
            dayOfWeek,
            listener);
    }

    private static void showDialog(
        Context context,
        String dialogName,
        LocalTime initialTime,
        String initialDescription,
        int initialDayOfWeek,
        WeekProcedureListener listener)
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

        editText.setText(initialDescription);

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
            int hourOfDay = timePicker.getHour();
            int minute = timePicker.getMinute();
            String description = editText.getText().toString().trim();
            int weekDayIndex = weekdaySpinner.getSelectedItemPosition() + 1;

            listener.onProcedureComplete(
                new Procedure(description, new LocalTime(hourOfDay, minute)),
                weekDayIndex);

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new EditTextButtonEnabledLink(
            buttonSave,
            new EditTextCriteria(editText, EditTextCriteria.IS_NOT_EMPTY));

        alertDialog.show();
    }
}
