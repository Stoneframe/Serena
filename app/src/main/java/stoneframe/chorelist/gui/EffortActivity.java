package stoneframe.chorelist.gui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import stoneframe.chorelist.R;

public class EffortActivity extends AppCompatActivity {

    private EditText mondayEditText;
    private EditText tuesdayEditText;
    private EditText wednesdayEditText;
    private EditText thursdayEditText;
    private EditText fridayEditText;
    private EditText saturdayEditText;
    private EditText sundayEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effort);

        mondayEditText = (EditText)findViewById(R.id.mondayEditText);
        tuesdayEditText = (EditText)findViewById(R.id.tuesdayEditText);
        wednesdayEditText = (EditText)findViewById(R.id.wednesdayEditText);
        thursdayEditText = (EditText)findViewById(R.id.thursdayEditText);
        fridayEditText = (EditText)findViewById(R.id.fridayEditText);
        saturdayEditText = (EditText)findViewById(R.id.saturdayEditText);
        sundayEditText = (EditText)findViewById(R.id.sundayEditText);

        Intent intent = getIntent();

        mondayEditText.setText(Integer.toString(intent.getIntExtra("Monday", 0)));
        tuesdayEditText.setText(Integer.toString(intent.getIntExtra("Tuesday", 0)));
        wednesdayEditText.setText(Integer.toString(intent.getIntExtra("Wednesday", 0)));
        thursdayEditText.setText(Integer.toString(intent.getIntExtra("Thursday", 0)));
        fridayEditText.setText(Integer.toString(intent.getIntExtra("Friday", 0)));
        saturdayEditText.setText(Integer.toString(intent.getIntExtra("Saturday", 0)));
        sundayEditText.setText(Integer.toString(intent.getIntExtra("Sunday", 0)));
    }

    public void onOkClick(View view) {
        int monday = Integer.parseInt(mondayEditText.getText().toString());
        int tuesday = Integer.parseInt(tuesdayEditText.getText().toString());
        int wednesday = Integer.parseInt(wednesdayEditText.getText().toString());
        int thursday = Integer.parseInt(thursdayEditText.getText().toString());
        int friday = Integer.parseInt(fridayEditText.getText().toString());
        int saturday = Integer.parseInt(saturdayEditText.getText().toString());
        int sunday = Integer.parseInt(sundayEditText.getText().toString());

        Intent intent = new Intent();
        intent.putExtra("Monday", monday);
        intent.putExtra("Tuesday", tuesday);
        intent.putExtra("Wednesday", wednesday);
        intent.putExtra("Thursday", thursday);
        intent.putExtra("Friday", friday);
        intent.putExtra("Saturday", saturday);
        intent.putExtra("Sunday", sunday);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCancelClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

}
