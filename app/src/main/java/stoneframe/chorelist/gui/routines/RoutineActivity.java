package stoneframe.chorelist.gui.routines;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.routines.Routine;

public abstract class RoutineActivity<T extends Routine> extends AppCompatActivity
{
    public static final int ROUTINE_ACTION_ADD = 0;
    public static final int ROUTINE_ACTION_EDIT = 1;

    protected int action;

    protected GlobalState globalState;
    protected ChoreList choreList;

    protected T routine;

    protected EditText nameEditText;
    protected CheckBox enabledCheckBox;

    protected Button saveButton;
    protected Button removeButton;

    protected Button addProcedureButton;

    /**
     * @noinspection unchecked
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(getRoutineContentView());

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        globalState = GlobalState.getInstance();
        choreList = globalState.getChoreList();

        routine = (T)globalState.getActiveRoutine();
        routine.edit();

        nameEditText = findViewById(R.id.nameTextEdit);
        enabledCheckBox = findViewById(R.id.enabledCheckbox);

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveRoutine());

        removeButton = findViewById(R.id.removeButton);
        removeButton.setVisibility(action == ROUTINE_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);
        removeButton.setOnClickListener(v -> removeRoutine());

        addProcedureButton = findViewById(R.id.addProcedureButton);
        addProcedureButton.setOnClickListener(v -> addProcedure());

        nameEditText.setText(routine.getName());
        enabledCheckBox.setChecked(routine.isEnabled());

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(nameEditText, EditTextCriteria.IS_NOT_EMPTY));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                back();
            }
        });
    }

    protected abstract int getRoutineContentView();

    protected abstract void addProcedure();

    private void saveRoutine()
    {
        if (enabledCheckBox.isChecked() && !routine.isEnabled())
        {
            choreList.resetRoutine(routine);
        }

        routine.setName(nameEditText.getText().toString().trim());
        routine.setEnabled(enabledCheckBox.isChecked());
        routine.save();

        if (action == ROUTINE_ACTION_ADD)
        {
            choreList.addRoutine(routine);
        }

        choreList.save();

        setResult(RESULT_OK);
        finish();
    }

    private void removeRoutine()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove Routine",
            "Are you sure you want to remove the routine?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                choreList.removeRoutine(routine);
                choreList.save();

                setResult(RESULT_OK);
                finish();
            });
    }

    private void back()
    {
        routine.revert();
        finish();
    }
}
