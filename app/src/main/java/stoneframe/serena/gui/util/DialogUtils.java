package stoneframe.serena.gui.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import stoneframe.serena.R;

public class DialogUtils
{
    public static void showConfirmationDialog(
        Context context,
        String title,
        String message,
        ConfirmationDialogListener listener)
    {
        AlertDialog confirmationDialog = new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes", (dialog, which) -> listener.onResponse(true))
            .setNegativeButton("No", (dialog, which) -> listener.onResponse(false))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .create();

        confirmationDialog.setOnShowListener(
            ignored -> addButtonStartSpacing(
                context,
                confirmationDialog.getButton(AlertDialog.BUTTON_POSITIVE)));
        confirmationDialog.show();
    }

    public static void addButtonStartSpacing(Context context, Button button)
    {
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();

        if (layoutParams instanceof ViewGroup.MarginLayoutParams)
        {
            ViewGroup.MarginLayoutParams marginLayoutParams =
                (ViewGroup.MarginLayoutParams)layoutParams;
            marginLayoutParams.setMarginStart(
                context.getResources().getDimensionPixelSize(R.dimen.space_sm));
            button.setLayoutParams(marginLayoutParams);
        }
    }

    public static void styleAsPrimaryButton(Context context, Button button)
    {
        button.setBackgroundTintList(
            ContextCompat.getColorStateList(context, R.color.button_primary_background));
        button.setTextColor(
            ContextCompat.getColorStateList(context, R.color.button_primary_text));
    }

    public static void alignButtonEndWithDialogContent(Context context, Button button)
    {
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();

        if (!(layoutParams instanceof ViewGroup.MarginLayoutParams) ||
            !(button.getParent() instanceof View))
        {
            return;
        }

        int contentInset =
            context.getResources().getDimensionPixelSize(R.dimen.space_lg);
        int buttonPanelInset = ((View)button.getParent()).getPaddingEnd();

        ViewGroup.MarginLayoutParams marginLayoutParams =
            (ViewGroup.MarginLayoutParams)layoutParams;
        marginLayoutParams.setMarginEnd(Math.max(0, contentInset - buttonPanelInset));
        button.setLayoutParams(marginLayoutParams);
    }

    public static void showWarningDialog(Context context, String title, String message)
    {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", (dialogInterface, i) ->
            {})
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public static void showWarningDialog(
        Context context,
        String title,
        String message,
        Runnable callback)
    {
        new AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", (dialogInterface, i) -> callback.run())
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }

    public interface ConfirmationDialogListener
    {
        void onResponse(boolean isConfirmed);
    }
}

