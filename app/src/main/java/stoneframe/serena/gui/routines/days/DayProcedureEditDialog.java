package stoneframe.serena.gui.routines.days;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import stoneframe.serena.R;
import stoneframe.serena.gui.util.ButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.model.routines.Procedure;

public class DayProcedureEditDialog
{
    public static void create(Context context, final DayProcedureListener listener)
    {
        showDialog(context, "Create Procedure", new LocalTime(0, 0), "", false, listener);
    }

    public static void copy(
        Context context,
        Procedure procedure,
        final DayProcedureListener listener)
    {
        showDialog(
            context,
            "Copy Procedure",
            procedure.getTime(),
            procedure.getDescription(),
            procedure.hasAlarm(),
            listener
        );
    }

    public static void edit(
        Context context,
        Procedure procedure,
        final DayProcedureListener listener)
    {
        showDialog(
            context,
            "Edit Procedure",
            procedure.getTime(),
            procedure.getDescription(),
            procedure.hasAlarm(),
            listener
        );
    }

    private static void showDialog(
        Context context,
        String dialogName,
        LocalTime initialTime,
        String initialDescription,
        boolean initialHasAlarm,
        DayProcedureListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View dialogView = inflater.inflate(R.layout.dialog_procedure_edit, null);

        builder.setView(dialogView);

        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        final EditText descriptionText = dialogView.findViewById(R.id.editText);
        final CheckBox alarmCheckBox = dialogView.findViewById(R.id.alarmCheckBox);

        Button buttonSave = dialogView.findViewById(R.id.buttonSave);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        builder.setTitle(dialogName);

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

        if (initialDescription != null)
        {
            descriptionText.setText(initialDescription);
        }

        alarmCheckBox.setChecked(initialHasAlarm);

        final AlertDialog alertDialog = builder.create();

        buttonSave.setOnClickListener(v ->
        {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String customText = descriptionText.getText().toString().trim();

            boolean hasAlarm = alarmCheckBox.isChecked();

            listener.onProcedureComplete(new Procedure(
                customText,
                new LocalTime(hour, minute),
                hasAlarm));

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new ButtonEnabledLink(
            buttonSave,
            new EditTextCriteria(descriptionText, EditTextCriteria.IS_NOT_EMPTY));

        alertDialog.show();
    }

    public interface DayProcedureListener
    {
        void onProcedureComplete(Procedure procedure);
    }
}
