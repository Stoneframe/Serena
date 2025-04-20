package stoneframe.serena.gui.chores;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.stream.IntStream;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.enable.ButtonEnabledLink;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.Serena;
import stoneframe.serena.chores.Chore;
import stoneframe.serena.chores.ChoreManager;
import stoneframe.serena.chores.Repetition;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;

public class EffortActivity extends AppCompatActivity implements TextWatcher
{
    private EditText mondayEditText;
    private EditText tuesdayEditText;
    private EditText wednesdayEditText;
    private EditText thursdayEditText;
    private EditText fridayEditText;
    private EditText saturdayEditText;
    private EditText sundayEditText;

    private Button cancelButton;
    private Button saveButton;

    private TextView effortPerWeekTextView;

    private Serena serena;
    private WeeklyEffortTracker effortTracker;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effort);

        GlobalState globalState = GlobalState.getInstance();

        serena = globalState.getSerena();
        effortTracker = (WeeklyEffortTracker)serena.getChoreManager().getEffortTracker();

        mondayEditText = findViewById(R.id.mondayEditText);
        tuesdayEditText = findViewById(R.id.tuesdayEditText);
        wednesdayEditText = findViewById(R.id.wednesdayEditText);
        thursdayEditText = findViewById(R.id.thursdayEditText);
        fridayEditText = findViewById(R.id.fridayEditText);
        saturdayEditText = findViewById(R.id.saturdayEditText);
        sundayEditText = findViewById(R.id.sundayEditText);

        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        effortPerWeekTextView = findViewById(R.id.effort_per_week_text);

        mondayEditText.setText(Integer.toString(effortTracker.getMonday()));
        tuesdayEditText.setText(Integer.toString(effortTracker.getTuesday()));
        wednesdayEditText.setText(Integer.toString(effortTracker.getWednesday()));
        thursdayEditText.setText(Integer.toString(effortTracker.getThursday()));
        fridayEditText.setText(Integer.toString(effortTracker.getFriday()));
        saturdayEditText.setText(Integer.toString(effortTracker.getSaturday()));
        sundayEditText.setText(Integer.toString(effortTracker.getSunday()));

        mondayEditText.addTextChangedListener(this);
        tuesdayEditText.addTextChangedListener(this);
        wednesdayEditText.addTextChangedListener(this);
        thursdayEditText.addTextChangedListener(this);
        fridayEditText.addTextChangedListener(this);
        saturdayEditText.addTextChangedListener(this);
        sundayEditText.addTextChangedListener(this);

        cancelButton.setOnClickListener(v -> onCancelClick());
        saveButton.setOnClickListener(v -> onSaveClick());

        new ButtonEnabledLink(
            saveButton,
            new EditTextCriteria(mondayEditText, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(tuesdayEditText, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(wednesdayEditText, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(thursdayEditText, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(fridayEditText, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(saturdayEditText, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(sundayEditText, EditTextCriteria.IS_VALID_INT));

        updateEffortPerWeekText();
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

    private void onSaveClick()
    {
        effortTracker.setMonday(getEffort(mondayEditText));
        effortTracker.setTuesday(getEffort(tuesdayEditText));
        effortTracker.setWednesday(getEffort(wednesdayEditText));
        effortTracker.setThursday(getEffort(thursdayEditText));
        effortTracker.setFriday(getEffort(fridayEditText));
        effortTracker.setSaturday(getEffort(saturdayEditText));
        effortTracker.setSunday(getEffort(sundayEditText));

        serena.save();

        setResult(RESULT_OK);
        finish();
    }

    private void onCancelClick()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    @SuppressLint("DefaultLocale")
    private void updateEffortPerWeekText()
    {
        GlobalState globalState = (GlobalState)getApplication();

        ChoreManager choreManager = globalState.getSerena().getChoreManager();

        double totalEffortFromChoresPerWeek = choreManager.getAllChores()
            .stream()
            .filter(Chore::isEnabled)
            .map(Chore::getRepetition)
            .mapToDouble(Repetition::getEffortPerWeek)
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
}
