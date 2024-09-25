package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class MyExceptionHandler implements Thread.UncaughtExceptionHandler
{
    private final Context context;

    public MyExceptionHandler(Context context)
    {
        this.context = context;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable)
    {
        Log.e("UncaughtException", "An uncaught exception occurred", throwable);

        new Thread(() ->
        {
            Looper.prepare();

            TextView textView = new TextView(context);
            textView.setPadding(30, 30, 30, 30);
            textView.setText(throwable + "\n\n" + Log.getStackTraceString(throwable));

            ScrollView scrollView = new ScrollView(context);
            scrollView.addView(textView);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("An Error Occurred")
                .setView(scrollView)
                .setPositiveButton("Close", (dialog, which) ->
                {
                    dialog.dismiss();
                    System.exit(1);
                })
                .show();

            Looper.loop();
        }).start();
    }
}

