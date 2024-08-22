package stoneframe.chorelist.gui.routines.days;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;

import org.joda.time.LocalTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.routines.Procedure;

public class DayProcedureEditDialog
{
    public interface DayProcedureListener
    {
        void onProcedureComplete(Procedure procedure);
    }

    public static void create(Context context, final DayProcedureListener listener)
    {
        showDialog(context, "Create Procedure", new LocalTime(0, 0), "", listener);
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
            listener
        );
    }

    private static void showDialog(
        Context context,
        String dialogName,
        LocalTime initialTime,
        String initialDescription,
        DayProcedureListener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);

        View dialogView = inflater.inflate(R.layout.dialog_procedure_edit, null);

        builder.setView(dialogView);

        final TimePicker timePicker = dialogView.findViewById(R.id.timePicker);
        final EditText descriptionText = dialogView.findViewById(R.id.editText);

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

        final AlertDialog alertDialog = builder.create();

        buttonSave.setOnClickListener(v ->
        {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            String customText = descriptionText.getText().toString();

            listener.onProcedureComplete(new Procedure(customText, new LocalTime(hour, minute)));

            alertDialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> alertDialog.dismiss());

        new EditTextButtonEnabledLink(
            buttonSave,
            new EditTextCriteria(descriptionText, EditTextCriteria.IS_NOT_EMPTY));

        alertDialog.show();
    }
}
