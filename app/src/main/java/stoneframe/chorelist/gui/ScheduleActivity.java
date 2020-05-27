package stoneframe.chorelist.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.R;

public class ScheduleActivity extends AppCompatActivity
{
    private EditText scheduleText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        Intent intent = getIntent();

        scheduleText = findViewById(R.id.scheduleText);
        scheduleText.setText(intent.getStringExtra("Schedule"));
    }

    public void onSaveClick(View view)
    {
        Intent intent = new Intent();

        intent.putExtra("Schedule", scheduleText.getText().toString());

        setResult(RESULT_OK, intent);
        finish();
    }
}
