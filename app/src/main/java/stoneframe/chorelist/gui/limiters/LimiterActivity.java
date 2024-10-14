package stoneframe.chorelist.gui.limiters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.BiConsumer;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.gui.util.SimpleListAdapter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.limiters.CustomExpenditureType;
import stoneframe.chorelist.model.limiters.ExpenditureType;
import stoneframe.chorelist.model.limiters.Limiter;
import stoneframe.chorelist.model.limiters.LimiterEditor;

public class LimiterActivity extends AppCompatActivity implements LimiterEditor.LimiterEditorListener
{
    private TextView textViewName;
    private TextView textViewExpenditureAvailable;
    private Spinner spinnerExpenditureType;
    private EditText editTextAmount;
    private Button buttonNewExpenditureType;
    private Button buttonEditExpenditureType;
    private Button buttonRemoveExpenditureType;
    private Button buttonAddExpenditure;
    private Button buttonSettings;
    private Button buttonDone;

    private SimpleListAdapter<ExpenditureType> expenditureTypeAdapter;

    private ChoreList choreList;

    private LimiterEditor limiterEditor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_remove)
        {
            removeLimiter();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limiter);

        setTitle("Limiter");

        Limiter limiter = GlobalState.getInstance().getActiveLimiter();

        choreList = GlobalState.getInstance().getChoreList();

        limiterEditor = choreList.getLimiterManager().getLimiterEditor(limiter);

        textViewName = findViewById(R.id.textViewName);

        textViewExpenditureAvailable = findViewById(R.id.textViewExpenditureAvailable);
        spinnerExpenditureType = findViewById(R.id.spinnerExpenditureType);
        editTextAmount = findViewById(R.id.editTextAmount);

        buttonNewExpenditureType = findViewById(R.id.buttonNewExpenditureType);
        buttonEditExpenditureType = findViewById(R.id.buttonEditExpenditureType);
        buttonRemoveExpenditureType = findViewById(R.id.buttonRemoveExpenditureType);

        buttonAddExpenditure = findViewById(R.id.buttonAddCalories);
        buttonSettings = findViewById(R.id.buttonSettings);

        buttonDone = findViewById(R.id.buttonDone);

        expenditureTypeAdapter = new SimpleListAdapter<>(
            this,
            limiterEditor::getExpenditureTypes,
            ExpenditureType::getName,
            l -> "",
            l -> "");

        spinnerExpenditureType.setAdapter(expenditureTypeAdapter);
        spinnerExpenditureType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                updateSelectedExpenditureType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        editTextAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        buttonNewExpenditureType.setOnClickListener(v -> addExpenditureType());
        buttonEditExpenditureType.setOnClickListener(v -> editExpenditureType());
        buttonRemoveExpenditureType.setOnClickListener(v -> removeExpenditureType());
        buttonAddExpenditure.setOnClickListener(v -> addExpenditure());

        buttonSettings.setOnClickListener(v -> showSettingsDialog());

        buttonDone.setOnClickListener(v -> finish());

        new EditTextButtonEnabledLink(
            buttonAddExpenditure,
            new EditTextCriteria(editTextAmount, EditTextCriteria.IS_VALID_INT));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        limiterEditor.addListener(this);

        updateName();
        updateAvailable();
        updateHint();
        updateSelectedExpenditureType();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        limiterEditor.removeListener(this);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void nameChanged()
    {
        updateName();
    }

    @Override
    public void unitChanged()
    {
        updateAvailable();
        updateHint();
    }

    @Override
    public void isQuickChanged(boolean isAllowed)
    {
        expenditureTypeAdapter.notifyDataSetChanged();

        if (spinnerExpenditureType.getSelectedItemPosition() != 0)
        {
            int position = limiterEditor.isQuickAllowed()
                ? spinnerExpenditureType.getSelectedItemPosition() + 1
                : spinnerExpenditureType.getSelectedItemPosition() - 1;

            spinnerExpenditureType.setSelection(position);
        }
        else if (isAllowed)
        {
            spinnerExpenditureType.setSelection(1);
        }

        updateSelectedExpenditureType();
    }

    @Override
    public void incrementPerDayChanged()
    {

    }

    @Override
    public void expenditureTypesChanged()
    {
        expenditureTypeAdapter.notifyDataSetChanged();
    }

    @Override
    public void expenditureTypeAdded(CustomExpenditureType expenditureType)
    {
        expenditureTypeAdapter.notifyDataSetChanged();

        int position = limiterEditor.getExpenditureTypes().indexOf(expenditureType);

        spinnerExpenditureType.setSelection(position);

        updateSelectedExpenditureType();
    }

    @Override
    public void expenditureTypeEdited(CustomExpenditureType expenditureType)
    {
        expenditureTypeAdapter.notifyDataSetChanged();

        updateSelectedExpenditureType();
    }

    @Override
    public void expenditureTypeRemoved(CustomExpenditureType expenditureType)
    {
        expenditureTypeAdapter.notifyDataSetChanged();

        spinnerExpenditureType.setSelection(0);

        updateSelectedExpenditureType();
    }

    @Override
    public void expenditureAdded()
    {
        updateAvailable();
    }

    @Override
    public void availableChanged()
    {
        updateAvailable();
    }

    private void addExpenditureType()
    {
        showExpenditureTypeDialog(null, null, (name, calories) ->
        {
            limiterEditor.addExpenditureType(new CustomExpenditureType(name, calories));
            choreList.save();
        });
    }

    private void editExpenditureType()
    {
        CustomExpenditureType expenditureType = (CustomExpenditureType)spinnerExpenditureType.getSelectedItem();

        assert expenditureType != null;

        showExpenditureTypeDialog(
            expenditureType.getName(),
            expenditureType.getAmount(),
            (name, amount) ->
            {
                limiterEditor.setExpenditureTypeName(expenditureType, name);
                limiterEditor.setExpenditureTypeAmount(expenditureType, amount);

                choreList.save();
            });
    }

    private void removeExpenditureType()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove expenditure type",
            "Are you sure you want to remove the expenditure type?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                CustomExpenditureType expenditureType =
                    (CustomExpenditureType)spinnerExpenditureType.getSelectedItem();

                assert expenditureType != null;

                limiterEditor.removeExpenditureType(expenditureType);

                choreList.save();
            });
    }

    private void addExpenditure()
    {
        ExpenditureType expenditureType = (ExpenditureType)spinnerExpenditureType.getSelectedItem();

        int enteredExpenditureAmount = expenditureType.isQuick()
            ? Integer.parseInt(editTextAmount.getText().toString())
            : expenditureType.getAmount();

        limiterEditor.addExpenditure(expenditureType.getName(), enteredExpenditureAmount);

        choreList.save();
    }

    private void removeLimiter()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove Limiter",
            "Are you sure you want to remove the limiter?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                limiterEditor.delete();
                choreList.save();

                finish();
            });
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedExpenditureType()
    {
        ExpenditureType selectedExpenditureType = (ExpenditureType)spinnerExpenditureType.getSelectedItem();

        assert selectedExpenditureType != null;

        if (selectedExpenditureType.isQuick())
        {
            editTextAmount.setText("");
            editTextAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        else
        {
            editTextAmount.setText(Integer.toString(selectedExpenditureType.getAmount()));
            editTextAmount.setInputType(InputType.TYPE_NULL);
        }

        buttonEditExpenditureType.setEnabled(!selectedExpenditureType.isQuick());
        buttonRemoveExpenditureType.setEnabled(!selectedExpenditureType.isQuick());
    }

    private void updateName()
    {
        textViewName.setText(limiterEditor.getName());
    }

    private void updateAvailable()
    {
        textViewExpenditureAvailable.setText(
            String.format("%s %s", limiterEditor.getAvailable(), limiterEditor.getUnit()));
    }

    private void updateHint()
    {
        String hint = limiterEditor.getUnit().isEmpty() ? "amount" : limiterEditor.getUnit();

        editTextAmount.setHint(hint);
    }

    private void showSettingsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_limiter_settings, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextUnit = dialogView.findViewById(R.id.editTextUnit);
        EditText editTextIncrementPerDay = dialogView.findViewById(R.id.editTextExpenditurePerDay);
        EditText editTextMaxValue = dialogView.findViewById(R.id.editTextMaxValue);
        CheckBox checkBoxAllowQuick = dialogView.findViewById(R.id.checkBoxAllowQuick);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        editTextName.setText(limiterEditor.getName());
        editTextUnit.setText(limiterEditor.getUnit());
        editTextIncrementPerDay.setText(String.valueOf(limiterEditor.getIncrementPerDay()));
        editTextMaxValue.setText(limiterEditor.hasMaxValue() ? Integer.toString(limiterEditor.getMaxValue()) : "");
        checkBoxAllowQuick.setChecked(limiterEditor.isQuickAllowed());
        checkBoxAllowQuick.setEnabled(limiterEditor.isQuickDisableable());

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonOk.setOnClickListener(v ->
        {
            String incrementPerDay = editTextIncrementPerDay.getText().toString();

            if (!incrementPerDay.isEmpty())
            {
                limiterEditor.setName(editTextName.getText().toString());
                limiterEditor.setUnit(editTextUnit.getText().toString());
                limiterEditor.setMaxValue(getMaxValue(editTextMaxValue));
                limiterEditor.setAllowQuick(checkBoxAllowQuick.isChecked());
                limiterEditor.setIncrementPerDay(Integer.parseInt(incrementPerDay));

                choreList.save();

                dialog.dismiss();
            }
        });

        dialog.show();

        new EditTextButtonEnabledLink(
            buttonOk,
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextIncrementPerDay, EditTextCriteria.IS_VALID_INT));
    }

    private static @Nullable Integer getMaxValue(EditText editTextMaxValue)
    {
        String maxValueStr = editTextMaxValue.getText().toString();
        return maxValueStr.isEmpty() ? null : Integer.parseInt(maxValueStr);
    }

    @SuppressLint("SetTextI18n")
    private void showExpenditureTypeDialog(
        String initialName,
        Integer initialCalories,
        BiConsumer<String, Integer> okClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_expenditure_type, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        if (initialName != null) editTextName.setText(initialName);
        if (initialCalories != null) editTextAmount.setText(initialCalories.toString());

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonOk.setOnClickListener(v ->
        {
            String name = editTextName.getText().toString();
            int amount = Integer.parseInt(editTextAmount.getText().toString());

            dialog.dismiss();

            okClickListener.accept(name, amount);
        });

        dialog.show();

        new EditTextButtonEnabledLink(
            dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextAmount, EditTextCriteria.IS_VALID_INT));
    }
}