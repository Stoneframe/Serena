package stoneframe.chorelist.gui.routines;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.EditActivity;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.routines.Routine;

public abstract class EditRoutineActivity<T extends Routine<?>> extends EditActivity
{
    protected EditText nameEditText;
    protected CheckBox enabledCheckBox;

    protected Button addProcedureButton;

    protected T routine;

    @Override
    protected int getActivityLayoutId()
    {
        return getRoutineContentView();
    }

    @Override
    protected abstract String getActivityTitle();

    @Override
    protected String getEditedObjectName()
    {
        return "Routine";
    }

    @Override
    protected void createActivity()
    {
        routine = (T)globalState.getActiveRoutine();
        routine.edit();

        nameEditText = findViewById(R.id.nameTextEdit);
        enabledCheckBox = findViewById(R.id.enabledCheckbox);

        addProcedureButton = findViewById(R.id.addProcedureButton);

        nameEditText.setText(routine.getName());
        enabledCheckBox.setChecked(routine.isEnabled());

        addProcedureButton.setOnClickListener(v -> addProcedure());

        createSpecialisedActivity();

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(nameEditText, EditTextCriteria.IS_NOT_EMPTY));
    }

    @Override
    protected void onCancel()
    {
        routine.revert();
    }

    @Override
    protected void onSave(int action)
    {
        if (enabledCheckBox.isChecked() && !routine.isEnabled())
        {
            choreList.resetRoutine(routine);
        }

        routine.setName(nameEditText.getText().toString().trim());
        routine.setEnabled(enabledCheckBox.isChecked());
        routine.save();

        if (action == ACTION_ADD)
        {
            choreList.addRoutine(routine);
        }

        choreList.save();
    }

    @Override
    protected void onRemove()
    {
        choreList.removeRoutine(routine);
        choreList.save();
    }

    protected abstract void createSpecialisedActivity();

    protected abstract int getRoutineContentView();

    protected abstract void addProcedure();
}
