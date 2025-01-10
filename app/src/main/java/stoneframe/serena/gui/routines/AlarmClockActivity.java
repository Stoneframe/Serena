package stoneframe.serena.gui.routines;


import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.routines.PendingProcedure;

public class AlarmClockActivity extends AppCompatActivity
{
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_clock);

        Serena serena = GlobalState.getInstance().getSerena();

        List<PendingProcedure> procedures = serena.getRoutineManager()
            .getPendingProcedures()
            .stream()
            .filter(PendingProcedure::hasAlarm)
            .collect(Collectors.toList());

        TextView messageTextView = findViewById(R.id.alarm_message);
        Button stopButton = findViewById(R.id.stop_button);

        String message = procedures.stream()
            .map(PendingProcedure::getDescription)
            .collect(Collectors.joining("\r\n"));

        messageTextView.setText(message);

        stopButton.setOnClickListener(v ->
        {
            stopAlarm();
            finish();
        });

        startAlarmSound();
    }

    private void startAlarmSound()
    {
        mediaPlayer = MediaPlayer.create(
            this,
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM));
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    private void stopAlarm()
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}

