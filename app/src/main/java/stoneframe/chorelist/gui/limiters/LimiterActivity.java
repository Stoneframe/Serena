package stoneframe.chorelist.gui.limiters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.BiConsumer;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.SimpleListAdapter;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.limiters.CustomExpenditureType;
import stoneframe.chorelist.model.limiters.ExpenditureType;
import stoneframe.chorelist.model.limiters.LimiterEditor;

public class LimiterActivity extends AppCompatActivity
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
    private Button buttonRemove;

    private ChoreList choreList;

    private LimiterEditor limiterEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limiter);

        choreList = GlobalState.getInstance().getChoreList();

        limiterEditor = choreList.getLimiterEditor(GlobalState.getInstance().getActiveLimiter());

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
        buttonRemove = findViewById(R.id.buttonRemove);

        updateUIComponents();

        textViewName.setText(limiterEditor.getName());

        SimpleListAdapter<ExpenditureType> expenditureTypeAdapter = new SimpleListAdapter<>(
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

        buttonNewExpenditureType.setOnClickListener(v ->
            showExpenditureTypeDialog(null, null, (name, calories) ->
            {
                limiterEditor.addExpenditureType(new CustomExpenditureType(name, calories));
                choreList.save();
            }));

        buttonEditExpenditureType.setOnClickListener(v ->
        {
            CustomExpenditureType expenditureType = (CustomExpenditureType)spinnerExpenditureType.getSelectedItem();

            assert expenditureType != null;

            showExpenditureTypeDialog(
                expenditureType.getName(),
                expenditureType.getAmount(),
                (name, expenditure) ->
                {
                    expenditureType.setName(name);
                    expenditureType.setExpenditure(expenditure);

                    choreList.save();

                    updateSelectedExpenditureType();

                    expenditureTypeAdapter.notifyDataSetChanged();
                });
        });

        buttonRemoveExpenditureType.setOnClickListener(v ->
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

                    spinnerExpenditureType.setSelection(0);
                }));

        buttonAddExpenditure.setOnClickListener(v ->
        {
            ExpenditureType expenditureType = (ExpenditureType)spinnerExpenditureType.getSelectedItem();

            int enteredExpenditureAmount = expenditureType.isQuick()
                ? Integer.parseInt(editTextAmount.getText().toString())
                : expenditureType.getAmount();

            limiterEditor.addExpenditure(expenditureType.getName(), enteredExpenditureAmount);

            choreList.save();

            if (expenditureType.isQuick())
            {
                editTextAmount.setText("");
            }

            updateUIComponents();
        });

        buttonSettings.setOnClickListener(v -> showSettingsDialog());

        buttonDone.setOnClickListener(v -> finish());

        buttonRemove.setOnClickListener(v ->
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
                }));

        new EditTextButtonEnabledLink(
            buttonAddExpenditure,
            new EditTextCriteria(editTextAmount, EditTextCriteria.IS_VALID_INT));
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedExpenditureType()
    {
        ExpenditureType selectedExpenditureType = (ExpenditureType)spinnerExpenditureType.getSelectedItem();

        assert selectedExpenditureType != null;

        if (!selectedExpenditureType.isQuick())
        {
            editTextAmount.setText(Integer.toString(selectedExpenditureType.getAmount()));
            editTextAmount.setInputType(InputType.TYPE_NULL);
        }
        else
        {
            editTextAmount.setText("");
            editTextAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }

        buttonEditExpenditureType.setEnabled(!selectedExpenditureType.isQuick());
        buttonRemoveExpenditureType.setEnabled(!selectedExpenditureType.isQuick());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
    }

    private void updateUIComponents()
    {
        textViewName.setText(limiterEditor.getName());
        editTextAmount.setHint(
            limiterEditor.getUnit().isEmpty() ? "amount" : limiterEditor.getUnit());
        textViewExpenditureAvailable.setText(
            String.format("%s %s", limiterEditor.getAvailable(), limiterEditor.getUnit()));
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
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        editTextName.setText(limiterEditor.getName());
        editTextUnit.setText(limiterEditor.getUnit());
        editTextIncrementPerDay.setText(String.valueOf(limiterEditor.getIncrementPerDay()));

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonOk.setOnClickListener(v ->
        {
            String incrementPerDay = editTextIncrementPerDay.getText().toString();

            if (!incrementPerDay.isEmpty())
            {
                limiterEditor.setName(editTextName.getText().toString());
                limiterEditor.setUnit(editTextUnit.getText().toString());

                limiterEditor.setIncrementPerDay(Integer.parseInt(incrementPerDay));
                choreList.save();

                dialog.dismiss();

                updateUIComponents();
            }
        });

        dialog.show();

        new EditTextButtonEnabledLink(
            buttonOk,
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextIncrementPerDay, EditTextCriteria.IS_VALID_INT));
    }

    @SuppressLint("SetTextI18n")
    private void showExpenditureTypeDialog(
        String initialName,
        Integer initialCalories,
        BiConsumer<String, Integer> okClickListener)
    {
        final EditText editTextName = new EditText(this);
        editTextName.setHint("Name");
        editTextName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        final EditText editTextAmount = new EditText(this);
        editTextAmount.setHint(limiterEditor.getUnit());
        editTextAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        if (initialName != null) editTextName.setText(initialName);
        if (initialCalories != null) editTextAmount.setText(initialCalories.toString());

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        layout.addView(editTextName);
        layout.addView(editTextAmount);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("Expenditure Type")
            .setView(layout)
            .setPositiveButton("OK", (d, which) ->
            {
                String name = editTextName.getText().toString();
                int amount = Integer.parseInt(editTextAmount.getText().toString());

                okClickListener.accept(name, amount);
            })
            .setNegativeButton("Cancel", null)
            .create();

        dialog.show();

        new EditTextButtonEnabledLink(
            dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextAmount, EditTextCriteria.IS_NOT_EMPTY));
    }
}