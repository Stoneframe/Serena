package stoneframe.serena.gui.settings;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import stoneframe.serena.R;

public class SettingsActivity extends AppCompatActivity
{
    private CheckBox silenceIsEnabledCheckBox;
    private EditText silenceStartTimeEditText;
    private EditText silenceStopTimeEditText;

    private Button saveButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        silenceIsEnabledCheckBox = findViewById(R.id.silenceEnabledCheckBox);
        silenceStartTimeEditText = findViewById(R.id.silenceStartTimeEditText);
        silenceStopTimeEditText = findViewById(R.id.silenceStopTimeEditText);

        saveButton = findViewById(R.id.saveButton);
    }
}
