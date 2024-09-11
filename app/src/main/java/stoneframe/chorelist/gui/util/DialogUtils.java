package stoneframe.chorelist.gui.util;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

public class DialogUtils
{
    public static void showConfirmationDialog(
        Context context,
        String title,
        String message,
        ConfirmationDialogListener listener)
    {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes", (dialog, which) -> listener.onResponse(true))
            .setNegativeButton("No", (dialog, which) -> listener.onResponse(false))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public static void showWarningDialog(Context context, String title, String message)
    {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", (dialogInterface, i) -> {})
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public interface ConfirmationDialogListener
    {
        void onResponse(boolean isConfirmed);
    }
}

