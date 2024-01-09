package stoneframe.chorelist.gui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;

public class ChoreActivity extends AppCompatActivity
{
    private DateTime next;
    private String description;
    private int priority;
    private int effort;
    private int intervalUnit;
    private int intervalLength;

    private DatePickerDialog datePickerDialog;

    private EditText nextEditText;
    private EditText descriptionEditText;
    private EditText priorityEditText;
    private EditText effortEditText;
    private Spinner intervalUnitSpinner;
    private EditText intervalLengthEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chore);

        Intent intent = getIntent();

        next = (DateTime)intent.getSerializableExtra("Next");
        description = intent.getStringExtra("Description");
        priority = intent.getIntExtra("Priority", 1);
        effort = intent.getIntExtra("Effort", 1);
        intervalUnit = intent.getIntExtra("IntervalUnit", 0);
        intervalLength = intent.getIntExtra("IntervalLength", 1);

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(
                android.widget.DatePicker view,
                int year,
                int month,
                int dayOfMonth)
            {
                next = new DateTime(year, month + 1, dayOfMonth, 0, 0);
                nextEditText.setText(next.toString("yyyy-MM-dd"));
            }
        }, next.getYear(), next.getMonthOfYear() - 1, next.getDayOfMonth());

        nextEditText = findViewById(R.id.nextEditText);
        descriptionEditText = findViewById(R.id.choreDescriptionEditText);
        priorityEditText = findViewById(R.id.priorityEditText);
        effortEditText = findViewById(R.id.effortEditText);
        intervalUnitSpinner = findViewById(R.id.intervalUnitSpinner);
        intervalLengthEditText = findViewById(R.id.intervalLengthEditText);

        intervalUnitSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
            new String[]{"Daily", "Weekly", "Monthly", "Yearly"}));

        nextEditText.setText(next.toString("yyyy-MM-dd"));
        descriptionEditText.setText(description);
        priorityEditText.setText(Integer.toString(priority), TextView.BufferType.EDITABLE);
        effortEditText.setText(Integer.toString(effort), TextView.BufferType.EDITABLE);
        intervalUnitSpinner.setSelection(intervalUnit);
        intervalLengthEditText.setText(Integer.toString(intervalLength), TextView.BufferType.EDITABLE);

        nextEditText.setInputType(InputType.TYPE_NULL);
        nextEditText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                datePickerDialog.show();
            }
        });
    }

    public void okClick(View view)
    {
        description = descriptionEditText.getText().toString();
        priority = Integer.parseInt(priorityEditText.getText().toString());
        effort = Integer.parseInt(effortEditText.getText().toString());
        intervalUnit = (int)intervalUnitSpinner.getSelectedItemId();
        intervalLength = Integer.parseInt(intervalLengthEditText.getText().toString());

        Intent intent = new Intent();
        intent.putExtra("Next", next);
        intent.putExtra("Description", description);
        intent.putExtra("Priority", priority);
        intent.putExtra("Effort", effort);
        intent.putExtra("IntervalUnit", intervalUnit);
        intent.putExtra("IntervalLength", intervalLength);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelClick(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }
}
