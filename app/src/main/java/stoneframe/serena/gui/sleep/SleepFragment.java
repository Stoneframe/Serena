package stoneframe.serena.gui.sleep;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.joda.time.Minutes;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.sleep.Sleep;
import stoneframe.serena.sleep.SleepManager;

@SuppressLint("SetTextI18n")
public class SleepFragment extends Fragment
{
    private GlobalState globalState;
    private Serena serena;
    private SleepManager sleepManager;

    private TextView sessionStartTimeTextView;
    private TextView sessionStopTimeTextView;
    private TextView sessionTotalTimeTextView;

    private TextView percentTextView;
    private Button toggleButton;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();
        sleepManager = serena.getSleepManager();

        View rootView = inflater.inflate(R.layout.fragment_sleep, container, false);

        sessionStartTimeTextView = rootView.findViewById(R.id.sessionStartTimeTextView);
        sessionStopTimeTextView = rootView.findViewById(R.id.sessionStopTimeTextView);
        sessionTotalTimeTextView = rootView.findViewById(R.id.sessionTotalTimeTextView);

        percentTextView = rootView.findViewById(R.id.percentTextView);
        toggleButton = rootView.findViewById(R.id.toggleButton);

        toggleButton.setOnClickListener(v ->
        {
            sleepManager.toggle();

            updateComponents();
        });

        updateComponents();

        return rootView;
    }

    private void updateComponents()
    {
        updatePreviousSession();
        updatePercentText();
        updatePercentTextColor();
        updateToggleButtonText();
    }

    private void updatePreviousSession()
    {
        Sleep.SleepSession session = sleepManager.getPreviousSession();

        if (session == null)
        {
            return;
        }


        sessionStartTimeTextView.setText(session.getStartTime().toString("yyyy-MM-dd HH:mm"));
        sessionStopTimeTextView.setText(session.getStopTime().toString("yyyy-MM-dd HH:mm"));
        sessionTotalTimeTextView.setText(formatMinutes(session.getSleepTime()));
    }

    private String formatMinutes(Minutes minutes)
    {
        int totalMinutes = minutes.getMinutes();
        int hours = totalMinutes / 60;
        int remainingMinutes = totalMinutes % 60;

        return String.format("%02d:%02d", hours, remainingMinutes);
    }

    private void updatePercentText()
    {
        percentTextView.setText(sleepManager.getPercent() + " %");
    }

    private void updatePercentTextColor()
    {
        if (sleepManager.isOnTrack())
        {
            percentTextView.setTextColor(Color.GREEN);
        }
        else
        {
            percentTextView.setTextColor(Color.RED);
        }
    }

    private void updateToggleButtonText()
    {
        if (sleepManager.getState() == Sleep.AWAKE)
        {
            toggleButton.setText("AWAKE");
        }
        else
        {
            toggleButton.setText("ASLEEP");
        }
    }
}