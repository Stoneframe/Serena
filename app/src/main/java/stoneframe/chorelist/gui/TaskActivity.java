package stoneframe.chorelist.gui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;

public class TaskActivity extends AppCompatActivity {

    private DateTime next;
    private String description;
    private int priority;
    private int effort;
    private int periodicity;
    private int frequency;

    private DatePickerDialog datePickerDialog;

    private EditText nextEditText;
    private EditText descriptionEditText;
    private EditText priorityEditText;
    private EditText effortEditText;
    private Spinner periodicitySpinner;
    private EditText frequencyEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();

        next = (DateTime) intent.getSerializableExtra("Next");
        description = intent.getStringExtra("Description");
        priority = intent.getIntExtra("Priority", 1);
        effort = intent.getIntExtra("Effort", 1);
        periodicity = intent.getIntExtra("Periodicity", 1);
        frequency = intent.getIntExtra("Frequency", 1);

        datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                next = new DateTime(year, month + 1, dayOfMonth, 0, 0);
                nextEditText.setText(next.toString("yyyy-MM-dd"));
            }
        }, next.getYear(), next.getMonthOfYear(), next.getDayOfMonth());

        nextEditText = (EditText) findViewById(R.id.nextEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        priorityEditText = (EditText) findViewById(R.id.priorityEditText);
        effortEditText = (EditText) findViewById(R.id.effortEditText);
        periodicitySpinner = (Spinner) findViewById(R.id.periodicitySpinner);
        frequencyEditText = (EditText) findViewById(R.id.frequencyEditText);

        periodicitySpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Daily", "Weekly", "Monthly", "Yearly"}));

        nextEditText.setText(next.toString("yyyy-MM-dd"));
        descriptionEditText.setText(description);
        priorityEditText.setText(Integer.toString(priority), TextView.BufferType.EDITABLE);
        effortEditText.setText(Integer.toString(effort), TextView.BufferType.EDITABLE);
        periodicitySpinner.setSelection(periodicity);
        frequencyEditText.setText(Integer.toString(frequency), TextView.BufferType.EDITABLE);

        nextEditText.setInputType(InputType.TYPE_NULL);
        nextEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog.show();
            }
        });
    }

    public void okClick(View view) {
        description = descriptionEditText.getText().toString();
        priority = Integer.parseInt(priorityEditText.getText().toString());
        effort = Integer.parseInt(effortEditText.getText().toString());
        periodicity = (int)periodicitySpinner.getSelectedItemId();
        frequency = Integer.parseInt(frequencyEditText.getText().toString());

        Intent intent = new Intent();
        intent.putExtra("Next", next);
        intent.putExtra("Description", description);
        intent.putExtra("Priority", priority);
        intent.putExtra("Effort", effort);
        intent.putExtra("Periodicity", periodicity);
        intent.putExtra("Frequency", frequency);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

}
