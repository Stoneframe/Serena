package stoneframe.serena.gui.balancers;

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
import stoneframe.serena.model.balancers.CustomExpenditureType;
import stoneframe.serena.model.balancers.ExpenditureType;
import stoneframe.serena.model.balancers.Balancer;
import stoneframe.serena.model.balancers.BalancerEditor;

public class BalanceActivity extends AppCompatActivity implements BalancerEditor.BalanceEditorListener
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

    private BalancerEditor balancerEditor;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.balancer_menu, menu);
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
            removeBalancer();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        balancerEditor.removeListener(this);

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
            int position = balancerEditor.isQuickAllowed()
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

        int position = balancerEditor.getExpenditureTypes().indexOf(expenditureType);

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
        setContentView(R.layout.activity_balancer);

        setTitle("Balancer");

        Balancer balancer = GlobalState.getInstance().getActiveBalancer();

        serena = GlobalState.getInstance().getSerena();

        balancerEditor = serena.getBalancerManager().getBalancerEditor(balancer);

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
            balancerEditor::getExpenditureTypes,
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
            () -> getFavoriteExpenditureTypes(balancer),
            ExpenditureType::getName)
            .withSecondaryTextFunction(t -> Integer.toString(t.getAmount()))
            .create();
        favoritesList.setAdapter(favoritesListAdapter);
        favoritesList.setOnItemClickListener((adapterView, view, position, id) ->
        {
            ExpenditureType expenditureType =
                (ExpenditureType)favoritesList.getItemAtPosition(position);

            DialogUtils.showConfirmationDialog(
                BalanceActivity.this,
                "Add expenditure",
                expenditureType.getName() + " (" + expenditureType.getAmount() + " " + balancer.getUnit() + ")",
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

        balancerEditor.addListener(this);

        updateName();
        updateAvailable();
        updateHint();
        updateSelectedExpenditureType();
    }

    private @NonNull List<ExpenditureType> getFavoriteExpenditureTypes(Balancer balancer)
    {
        return balancer.getExpenditureTypes()
            .stream()
            .filter(ExpenditureType::isFavorite)
            .collect(Collectors.toList());
    }

    private void addExpenditureType()
    {
        showExpenditureTypeDialog(null, null, null, (name, calories, isFavorite) ->
        {
            balancerEditor.addExpenditureType(new CustomExpenditureType(name, calories, isFavorite));
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
                balancerEditor.setExpenditureTypeName(expenditureType, name);
                balancerEditor.setExpenditureTypeAmount(expenditureType, amount);
                balancerEditor.setFavorite(expenditureType, isFavorite);

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

                balancerEditor.removeExpenditureType(expenditureType);

                serena.save();
            });
    }

    private void addExpenditure(ExpenditureType expenditureType)
    {
        int enteredExpenditureAmount = expenditureType.isQuick()
            ? Integer.parseInt(editTextAmount.getText().toString())
            : expenditureType.getAmount();

        balancerEditor.addExpenditure(expenditureType.getName(), enteredExpenditureAmount);

        serena.save();
    }

    private void removeBalancer()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove Balancer",
            "Are you sure you want to remove the balancer?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                balancerEditor.delete();
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
        textViewName.setText(balancerEditor.getName());
    }

    private void updateAvailable()
    {
        textViewExpenditureAvailable.setText(
            String.format("%s %s", balancerEditor.getAvailable(), balancerEditor.getUnit()));
    }

    private void updateHint()
    {
        String hint = balancerEditor.getUnit().isEmpty() ? "amount" : balancerEditor.getUnit();

        editTextAmount.setHint(hint);
    }

    private void showSettingsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_balancer_settings, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextUnit = dialogView.findViewById(R.id.editTextUnit);
        EditText editTextIncrementPerDay = dialogView.findViewById(R.id.editTextExpenditurePerDay);
        EditText editTextMaxValue = dialogView.findViewById(R.id.editTextMaxValue);
        CheckBox checkBoxAllowQuick = dialogView.findViewById(R.id.checkBoxAllowQuick);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        editTextName.setText(balancerEditor.getName());
        editTextUnit.setText(balancerEditor.getUnit());
        editTextIncrementPerDay.setText(String.valueOf(balancerEditor.getIncrementPerDay()));
        editTextMaxValue.setText(balancerEditor.hasMaxValue()
            ? Integer.toString(balancerEditor.getMaxValue())
            : "");
        checkBoxAllowQuick.setChecked(balancerEditor.isQuickAllowed());
        checkBoxAllowQuick.setEnabled(balancerEditor.isQuickDisableable());

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonOk.setOnClickListener(v ->
        {
            String incrementPerDay = editTextIncrementPerDay.getText().toString();

            if (!incrementPerDay.isEmpty())
            {
                balancerEditor.setName(editTextName.getText().toString());
                balancerEditor.setUnit(editTextUnit.getText().toString());
                balancerEditor.setMaxValue(getMaxValue(editTextMaxValue));
                balancerEditor.setAllowQuick(checkBoxAllowQuick.isChecked());
                balancerEditor.setIncrementPerDay(Integer.parseInt(incrementPerDay));

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