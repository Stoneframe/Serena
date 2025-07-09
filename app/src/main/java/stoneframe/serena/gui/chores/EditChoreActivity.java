package stoneframe.serena.gui.chores;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.LocalDate;

import java.util.Arrays;
import java.util.stream.Stream;

import stoneframe.serena.R;
import stoneframe.serena.chores.Chore;
import stoneframe.serena.chores.ChoreEditor;
import stoneframe.serena.chores.DaysInWeekRepetition;
import stoneframe.serena.chores.IntervalRepetition;
import stoneframe.serena.chores.Repetition;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.enable.CheckboxCriteria;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.gui.util.enable.ViewGroupCriteria;

public class EditChoreActivity extends EditActivity
{
    private CheckBox enabledCheckbox;
    private EditText descriptionEditText;
    private EditText priorityEditText;
    private EditText effortEditText;
    private Spinner repetitionTypeSpinner;

    private RepetitionView intervalRepetitionView;
    private RepetitionView daysOfWeekRepetitionView;

    private ChoreEditor choreEditor;

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_chore;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Chore";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Chore";
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void createActivity()
    {
        Chore chore = globalState.getActiveChore();

        choreEditor = serena.getChoreManager().getChoreEditor(chore);

        enabledCheckbox = findViewById(R.id.enableCheckbox);
        descriptionEditText = findViewById(R.id.choreDescriptionEditText);
        priorityEditText = findViewById(R.id.priorityEditText);
        effortEditText = findViewById(R.id.effortEditText);
        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);
        repetitionTypeSpinner = findViewById(R.id.repetitionTypeSpinner);

        enabledCheckbox.setChecked(choreEditor.isEnabled());
        descriptionEditText.setText(choreEditor.getDescription());
        effortEditText.setText(
            Integer.toString(choreEditor.getEffort()),
            TextView.BufferType.EDITABLE);
        priorityEditText.setText(
            Integer.toString(choreEditor.getPriority()),
            TextView.BufferType.EDITABLE);

        repetitionTypeSpinner.setAdapter(new ArrayAdapter<>(
            EditChoreActivity.this,
            android.R.layout.simple_list_item_1,
            new String[]{"Interval", "Days in week"}));
        repetitionTypeSpinner.setSelection(choreEditor.getRepetition().getRepetitionType());
        repetitionTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                choreEditor.setRepetitionType(position);

                setActiveRepetition(choreEditor.getRepetition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                repetitionTypeSpinner.setSelection(0);
            }
        });

        intervalRepetitionView = new IntervalRepetitionView();
        daysOfWeekRepetitionView = new DaysOfWeekRepetitionView();

        intervalRepetitionView.create();
        daysOfWeekRepetitionView.create();

        setActiveRepetition(choreEditor.getRepetition());
    }

    @Override
    protected void startActivity()
    {
    }

    @Override
    protected void stopActivity()
    {
    }

    @Override
    protected EnableCriteria[] getSaveEnabledCriteria()
    {
        EnableCriteria[] baseViewCriteria =
            {
                new EditTextCriteria(descriptionEditText, EditTextCriteria.IS_NOT_EMPTY),
                new EditTextCriteria(priorityEditText, EditTextCriteria.IS_NOT_EMPTY),
                new EditTextCriteria(effortEditText, EditTextCriteria.IS_NOT_EMPTY),
            };

        return Stream.concat(
                Arrays.stream(baseViewCriteria),
                Stream.concat(
                    Arrays.stream(intervalRepetitionView.getSaveEnabledCriteria()),
                    Arrays.stream(daysOfWeekRepetitionView.getSaveEnabledCriteria())))
            .toArray(EnableCriteria[]::new);
    }

    @Override
    protected boolean onCancel()
    {
        choreEditor.revert();

        return true;
    }

    @Override
    protected boolean onSave(int action)
    {
        boolean isEnabled = enabledCheckbox.isChecked();
        String description = descriptionEditText.getText().toString().trim();
        int priority = Integer.parseInt(priorityEditText.getText().toString());
        int effort = Integer.parseInt(effortEditText.getText().toString());

        choreEditor.setEnabled(isEnabled);
        choreEditor.setDescription(description);
        choreEditor.setPriority(priority);
        choreEditor.setEffort(effort);

        getActiveRepetition().save(choreEditor.getRepetition());

        choreEditor.save();

        return true;
    }

    @Override
    protected void onRemove()
    {
        choreEditor.remove();
    }

    private RepetitionView getActiveRepetition()
    {
        int repetitionType = choreEditor.getRepetition().getRepetitionType();

        switch (repetitionType)
        {
            case Repetition.Interval:
                return intervalRepetitionView;
            case Repetition.DaysInWeek:
                return daysOfWeekRepetitionView;
            default:
                throw new IllegalStateException("Unknown repetition type: " + repetitionType);
        }
    }

    private void setActiveRepetition(Repetition repetition)
    {
        switch (repetition.getRepetitionType())
        {
            case Repetition.Interval:
                intervalRepetitionView.show(repetition);
                daysOfWeekRepetitionView.hide();
                break;
            case Repetition.DaysInWeek:
                intervalRepetitionView.hide();
                daysOfWeekRepetitionView.show(repetition);
                break;
        }
    }

    private interface RepetitionView
    {
        void create();

        void show(Repetition repetition);

        void hide();

        void save(Repetition repetition);

        EnableCriteria[] getSaveEnabledCriteria();
    }

    private class IntervalRepetitionView implements RepetitionView
    {
        private ViewGroup intervalRepetitionView;

        private EditText nextEditText;
        private EditText intervalLengthEditText;
        private Spinner intervalUnitSpinner;

        @Override
        public void create()
        {
            intervalRepetitionView = findViewById(R.id.intervalRepetition);

            nextEditText = findViewById(R.id.nextEditText);
            nextEditText.setInputType(InputType.TYPE_NULL);
            nextEditText.setOnClickListener(view -> showNextDatePickerDialog());

            intervalLengthEditText = findViewById(R.id.intervalLengthEditText);

            intervalUnitSpinner = findViewById(R.id.intervalUnitSpinner);
            intervalUnitSpinner.setAdapter(new ArrayAdapter<>(
                EditChoreActivity.this,
                android.R.layout.simple_list_item_1,
                new String[]{"Days", "Weeks", "Months", "Years"}));
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void show(Repetition repetition)
        {
            IntervalRepetition intervalRepetition = (IntervalRepetition)repetition;

            nextEditText.setText(intervalRepetition.getNext().toString("yyyy-MM-dd"));

            intervalLengthEditText.setText(
                Integer.toString(intervalRepetition.getIntervalLength()),
                TextView.BufferType.EDITABLE);

            intervalUnitSpinner.setSelection(intervalRepetition.getIntervalUnit());

            intervalRepetitionView.setVisibility(View.VISIBLE);
        }

        @Override
        public void hide()
        {
            intervalRepetitionView.setVisibility(View.GONE);
        }

        @Override
        public void save(Repetition repetition)
        {
            IntervalRepetition intervalRepetition = (IntervalRepetition)repetition;

            LocalDate next = LocalDate.parse(nextEditText.getText().toString());
            int intervalLength = Integer.parseInt(intervalLengthEditText.getText().toString());
            int intervalUnit = (int)intervalUnitSpinner.getSelectedItemId();

            intervalRepetition.setNext(next);
            intervalRepetition.setIntervalLength(intervalLength);
            intervalRepetition.setIntervalUnit(intervalUnit);

            choreEditor.updateNext();
        }

        public EnableCriteria[] getSaveEnabledCriteria()
        {
            return new EnableCriteria[]{
                new ViewGroupCriteria(intervalRepetitionView, c -> true),
                new EditTextCriteria(
                    intervalLengthEditText,
                    e -> !isVisible() || EditTextCriteria.isValidInteger(e)),
            };
        }

        private boolean isVisible()
        {
            return intervalRepetitionView.getVisibility() == View.VISIBLE;
        }

        private void showNextDatePickerDialog()
        {
            IntervalRepetition repetition = (IntervalRepetition)choreEditor.getRepetition();

            LocalDate next = repetition.getNext();

            DatePickerDialog nextPickerDialog = new DatePickerDialog(
                EditChoreActivity.this,
                (v, year, month, dayOfMonth) ->
                {
                    LocalDate newNext = new LocalDate(year, month + 1, dayOfMonth);
                    nextEditText.setText(newNext.toString("yyyy-MM-dd"));
                },
                next.getYear(),
                next.getMonthOfYear() - 1,
                next.getDayOfMonth());

            nextPickerDialog.show();
        }
    }

    private class DaysOfWeekRepetitionView implements RepetitionView
    {
        private ViewGroup daysOfWeekRepetitionView;

        private CheckBox monCheckBox;
        private CheckBox tueCheckBox;
        private CheckBox wedCheckBox;
        private CheckBox thuCheckBox;
        private CheckBox friCheckBox;
        private CheckBox satCheckBox;
        private CheckBox sunCheckBox;

        @Override
        public void create()
        {
            daysOfWeekRepetitionView = findViewById(R.id.daysOfWeekRepetition);

            monCheckBox = findViewById(R.id.monCheckBox);
            tueCheckBox = findViewById(R.id.tueCheckBox);
            wedCheckBox = findViewById(R.id.wedCheckBox);
            thuCheckBox = findViewById(R.id.thuCheckBox);
            friCheckBox = findViewById(R.id.friCheckBox);
            satCheckBox = findViewById(R.id.satCheckBox);
            sunCheckBox = findViewById(R.id.sunCheckBox);
        }

        @Override
        public void show(Repetition repetition)
        {
            DaysInWeekRepetition daysInWeekRepetition = (DaysInWeekRepetition)repetition;

            monCheckBox.setChecked(daysInWeekRepetition.getMonday());
            tueCheckBox.setChecked(daysInWeekRepetition.getTuesday());
            wedCheckBox.setChecked(daysInWeekRepetition.getWednesday());
            thuCheckBox.setChecked(daysInWeekRepetition.getThursday());
            friCheckBox.setChecked(daysInWeekRepetition.getFriday());
            satCheckBox.setChecked(daysInWeekRepetition.getSaturday());
            sunCheckBox.setChecked(daysInWeekRepetition.getSunday());

            daysOfWeekRepetitionView.setVisibility(View.VISIBLE);
        }

        @Override
        public void hide()
        {
            daysOfWeekRepetitionView.setVisibility(View.GONE);
        }

        @Override
        public void save(Repetition repetition)
        {
            DaysInWeekRepetition daysInWeekRepetition = (DaysInWeekRepetition)repetition;

            daysInWeekRepetition.setMonday(monCheckBox.isChecked());
            daysInWeekRepetition.setTuesday(tueCheckBox.isChecked());
            daysInWeekRepetition.setWednesday(wedCheckBox.isChecked());
            daysInWeekRepetition.setThursday(thuCheckBox.isChecked());
            daysInWeekRepetition.setFriday(friCheckBox.isChecked());
            daysInWeekRepetition.setSaturday(satCheckBox.isChecked());
            daysInWeekRepetition.setSunday(sunCheckBox.isChecked());

            choreEditor.updateNext();
        }

        @Override
        public EnableCriteria[] getSaveEnabledCriteria()
        {
            return new EnableCriteria[]{
                new ViewGroupCriteria(daysOfWeekRepetitionView, c -> true),
                new CheckboxCriteria(monCheckBox, c -> !isVisible() || anyCheckboxChecked()),
                new CheckboxCriteria(tueCheckBox, c -> !isVisible() || anyCheckboxChecked()),
                new CheckboxCriteria(wedCheckBox, c -> !isVisible() || anyCheckboxChecked()),
                new CheckboxCriteria(thuCheckBox, c -> !isVisible() || anyCheckboxChecked()),
                new CheckboxCriteria(friCheckBox, c -> !isVisible() || anyCheckboxChecked()),
                new CheckboxCriteria(satCheckBox, c -> !isVisible() || anyCheckboxChecked()),
                new CheckboxCriteria(sunCheckBox, c -> !isVisible() || anyCheckboxChecked()),
            };
        }

        private boolean isVisible()
        {
            return daysOfWeekRepetitionView.getVisibility() == View.VISIBLE;
        }

        private boolean anyCheckboxChecked()
        {
            CheckBox[] allCheckboxes = {
                monCheckBox,
                tueCheckBox,
                wedCheckBox,
                thuCheckBox,
                friCheckBox,
                satCheckBox,
                sunCheckBox};

            return Arrays.stream(allCheckboxes).anyMatch(CompoundButton::isChecked);
        }
    }
}
