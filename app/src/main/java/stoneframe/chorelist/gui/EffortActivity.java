package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.stream.IntStream;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;

public class EffortActivity extends AppCompatActivity implements TextWatcher
{
    private EditText mondayEditText;
    private EditText tuesdayEditText;
    private EditText wednesdayEditText;
    private EditText thursdayEditText;
    private EditText fridayEditText;
    private EditText saturdayEditText;
    private EditText sundayEditText;

    private TextView effortPerWeekTextView;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effort);

        mondayEditText = findViewById(R.id.mondayEditText);
        tuesdayEditText = findViewById(R.id.tuesdayEditText);
        wednesdayEditText = findViewById(R.id.wednesdayEditText);
        thursdayEditText = findViewById(R.id.thursdayEditText);
        fridayEditText = findViewById(R.id.fridayEditText);
        saturdayEditText = findViewById(R.id.saturdayEditText);
        sundayEditText = findViewById(R.id.sundayEditText);

        effortPerWeekTextView = findViewById(R.id.effort_per_week_text);

        Intent intent = getIntent();

        mondayEditText.setText(Integer.toString(intent.getIntExtra("Monday", 0)));
        tuesdayEditText.setText(Integer.toString(intent.getIntExtra("Tuesday", 0)));
        wednesdayEditText.setText(Integer.toString(intent.getIntExtra("Wednesday", 0)));
        thursdayEditText.setText(Integer.toString(intent.getIntExtra("Thursday", 0)));
        fridayEditText.setText(Integer.toString(intent.getIntExtra("Friday", 0)));
        saturdayEditText.setText(Integer.toString(intent.getIntExtra("Saturday", 0)));
        sundayEditText.setText(Integer.toString(intent.getIntExtra("Sunday", 0)));

        mondayEditText.addTextChangedListener(this);
        tuesdayEditText.addTextChangedListener(this);
        wednesdayEditText.addTextChangedListener(this);
        thursdayEditText.addTextChangedListener(this);
        fridayEditText.addTextChangedListener(this);
        saturdayEditText.addTextChangedListener(this);
        sundayEditText.addTextChangedListener(this);

        updateEffortPerWeekText();
    }

    public void onOkClick(View view)
    {
        Intent intent = new Intent();
        intent.putExtra("Monday", getEffort(mondayEditText));
        intent.putExtra("Tuesday", getEffort(tuesdayEditText));
        intent.putExtra("Wednesday", getEffort(wednesdayEditText));
        intent.putExtra("Thursday", getEffort(thursdayEditText));
        intent.putExtra("Friday", getEffort(fridayEditText));
        intent.putExtra("Saturday", getEffort(saturdayEditText));
        intent.putExtra("Sunday", getEffort(sundayEditText));

        setResult(RESULT_OK, intent);
        finish();
    }

    private int getEffort(EditText textEdit)
    {
        try
        {
            return Integer.parseInt(textEdit.getText().toString());
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    public void onCancelClick(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {

    }

    @Override
    public void afterTextChanged(Editable s)
    {
        updateEffortPerWeekText();
    }

    @SuppressLint("DefaultLocale")
    private void updateEffortPerWeekText()
    {
        GlobalState globalState = (GlobalState)getApplication();

        ChoreList choreList = globalState.getChoreList();

        double totalEffortFromChoresPerWeek = choreList.getAllChores()
            .stream()
            .mapToDouble(c ->
            {
                switch (c.getIntervalUnit())
                {
                    case Chore.DAYS:
                        return (double)c.getEffort() / c.getIntervalLength() * 7;
                    case Chore.WEEKS:
                        return (double)c.getEffort() / c.getIntervalLength();
                    case Chore.MONTHS:
                        return (double)c.getEffort() / c.getIntervalLength() / 30 * 7;
                    case Chore.YEARS:
                        return (double)c.getEffort() / c.getIntervalLength() / 365 * 7;
                    default:
                        throw new IllegalStateException("Unknown interval unit " + c.getIntervalUnit());
                }
            })
            .sum();

        int totalEffortEntered = IntStream.of(
            getEffort(mondayEditText),
            getEffort(tuesdayEditText),
            getEffort(wednesdayEditText),
            getEffort(thursdayEditText),
            getEffort(fridayEditText),
            getEffort(saturdayEditText),
            getEffort(sundayEditText)).sum();

        effortPerWeekTextView.setText(String.format(
            "Required: %.1f, Entered: %d",
            totalEffortFromChoresPerWeek,
            totalEffortEntered));
    }
}
