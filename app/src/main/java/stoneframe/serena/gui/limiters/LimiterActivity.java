package stoneframe.serena.gui.limiters;

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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.EditTextButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.limiters.CustomExpenditureType;
import stoneframe.serena.model.limiters.ExpenditureType;
import stoneframe.serena.model.limiters.Limiter;
import stoneframe.serena.model.limiters.LimiterEditor;

public class LimiterActivity extends AppCompatActivity implements LimiterEditor.LimiterEditorListener
{
    private TextView textViewName;
    private TextView textViewExpenditureAvailable;

    private Spinner spinnerExpenditureType;
    private EditText editTextAmount;

    private ListView favoritesList;

    private Button buttonNewExpenditureType;
    private Button buttonEditExpenditureType;
    private Button buttonRemoveExpenditureType;
    private Button buttonAddExpenditure;
    private Button buttonDone;

    private SimpleListAdapter<ExpenditureType> expenditureTypeAdapter;
    private SimpleListAdapter<ExpenditureType> favoritesListAdapter;

    private Serena serena;

    private LimiterEditor limiterEditor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.limiter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        if (itemId == R.id.action_settings)
        {
            showSettingsDialog();
            return true;
        }

        if (itemId == R.id.action_remove)
        {
            removeLimiter();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
        favoritesListAdapter.notifyDataSetChanged();
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limiter);

        setTitle("Limiter");

        Limiter limiter = GlobalState.getInstance().getActiveLimiter();

        serena = GlobalState.getInstance().getSerena();

        limiterEditor = serena.getLimiterManager().getLimiterEditor(limiter);

        textViewName = findViewById(R.id.textViewName);

        textViewExpenditureAvailable = findViewById(R.id.textViewExpenditureAvailable);
        spinnerExpenditureType = findViewById(R.id.spinnerExpenditureType);
        editTextAmount = findViewById(R.id.editTextAmount);

        favoritesList = findViewById(R.id.listFavorites);

        buttonNewExpenditureType = findViewById(R.id.buttonNewExpenditureType);
        buttonEditExpenditureType = findViewById(R.id.buttonEditExpenditureType);
        buttonRemoveExpenditureType = findViewById(R.id.buttonRemoveExpenditureType);

        buttonAddExpenditure = findViewById(R.id.buttonAddCalories);

        buttonDone = findViewById(R.id.buttonDone);

        expenditureTypeAdapter = new SimpleListAdapterBuilder<>(
            this,
            limiterEditor::getExpenditureTypes,
            ExpenditureType::getName)
            .create();

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

        favoritesListAdapter = new SimpleListAdapterBuilder<>(
            this,
            () -> getFavoriteExpenditureTypes(limiter),
            ExpenditureType::getName)
            .withSecondaryTextFunction(t -> Integer.toString(t.getAmount()))
            .create();
        favoritesList.setAdapter(favoritesListAdapter);
        favoritesList.setOnItemClickListener((adapterView, view, position, id) ->
        {
            ExpenditureType expenditureType =
                (ExpenditureType)favoritesList.getItemAtPosition(position);

            DialogUtils.showConfirmationDialog(
                LimiterActivity.this,
                "Add expenditure",
                expenditureType.getName() + " (" + expenditureType.getAmount() + " " + limiter.getUnit() + ")",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    addExpenditure(expenditureType);
                });
        });

        buttonNewExpenditureType.setOnClickListener(v -> addExpenditureType());
        buttonEditExpenditureType.setOnClickListener(v -> editExpenditureType());
        buttonRemoveExpenditureType.setOnClickListener(v -> removeExpenditureType());
        buttonAddExpenditure.setOnClickListener(v -> addExpenditure((ExpenditureType)spinnerExpenditureType.getSelectedItem()));

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

    private @NonNull List<ExpenditureType> getFavoriteExpenditureTypes(Limiter limiter)
    {
        return limiter.getExpenditureTypes()
            .stream()
            .filter(ExpenditureType::isFavorite)
            .collect(Collectors.toList());
    }

    private void addExpenditureType()
    {
        showExpenditureTypeDialog(null, null, null, (name, calories, isFavorite) ->
        {
            limiterEditor.addExpenditureType(new CustomExpenditureType(name, calories, isFavorite));
            serena.save();
        });
    }

    private void editExpenditureType()
    {
        CustomExpenditureType expenditureType = (CustomExpenditureType)spinnerExpenditureType.getSelectedItem();

        assert expenditureType != null;

        showExpenditureTypeDialog(
            expenditureType.getName(),
            expenditureType.getAmount(),
            expenditureType.isFavorite(),
            (name, amount, isFavorite) ->
            {
                limiterEditor.setExpenditureTypeName(expenditureType, name);
                limiterEditor.setExpenditureTypeAmount(expenditureType, amount);
                limiterEditor.setFavorite(expenditureType, isFavorite);

                serena.save();
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

                serena.save();
            });
    }

    private void addExpenditure(ExpenditureType expenditureType)
    {
        int enteredExpenditureAmount = expenditureType.isQuick()
            ? Integer.parseInt(editTextAmount.getText().toString())
            : expenditureType.getAmount();

        limiterEditor.addExpenditure(expenditureType.getName(), enteredExpenditureAmount);

        serena.save();
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
                serena.save();

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
        editTextMaxValue.setText(limiterEditor.hasMaxValue()
            ? Integer.toString(limiterEditor.getMaxValue())
            : "");
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

                serena.save();

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
        Boolean initialIsFavorite,
        ExpenditureTypeOkListener okClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_expenditure_type, null);
        builder.setView(dialogView);

        CheckBox checkBoxFavorite = dialogView.findViewById(R.id.checkBoxFavorite);
        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextAmount = dialogView.findViewById(R.id.editTextAmount);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        if (initialName != null) editTextName.setText(initialName);
        if (initialCalories != null) editTextAmount.setText(initialCalories.toString());
        if (initialIsFavorite != null) checkBoxFavorite.setChecked(initialIsFavorite);

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        buttonOk.setOnClickListener(v ->
        {
            String name = editTextName.getText().toString();
            int amount = Integer.parseInt(editTextAmount.getText().toString());
            boolean isFavorite = checkBoxFavorite.isChecked();

            dialog.dismiss();

            okClickListener.onOkClick(name, amount, isFavorite);
        });

        dialog.show();

        new EditTextButtonEnabledLink(
            buttonOk,
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextAmount, EditTextCriteria.IS_VALID_INT));
    }

    private interface ExpenditureTypeOkListener
    {
        void onOkClick(String name, int amount, boolean isFavorite);
    }
}