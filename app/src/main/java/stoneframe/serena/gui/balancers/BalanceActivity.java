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
import stoneframe.serena.model.balancers.Balancer;
import stoneframe.serena.model.balancers.BalancerEditor;
import stoneframe.serena.model.balancers.CustomTransactionType;
import stoneframe.serena.model.balancers.TransactionType;

public class BalanceActivity extends AppCompatActivity implements BalancerEditor.BalanceEditorListener
{
    private TextView textViewName;
    private TextView textViewTransactionAvailable;

    private Spinner spinnerTransactionType;
    private EditText editTextAmount;

    private ListView favoritesList;

    private Button buttonNewTransactionType;
    private Button buttonEditTransactionType;
    private Button buttonRemoveTransactionType;
    private Button buttonAddTransaction;
    private Button buttonDone;

    private SimpleListAdapter<TransactionType> transactionTypeAdapter;
    private SimpleListAdapter<TransactionType> favoritesListAdapter;

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
        transactionTypeAdapter.notifyDataSetChanged();

        if (spinnerTransactionType.getSelectedItemPosition() != 0)
        {
            int position = balancerEditor.isQuickAllowed()
                ? spinnerTransactionType.getSelectedItemPosition() + 1
                : spinnerTransactionType.getSelectedItemPosition() - 1;

            spinnerTransactionType.setSelection(position);
        }
        else if (isAllowed)
        {
            spinnerTransactionType.setSelection(1);
        }

        updateSelectedTransactionType();
    }

    @Override
    public void isEnabledChanged(boolean isEnabled)
    {
        setActivityEnabled(isEnabled);
    }

    @Override
    public void changePerDayChanged()
    {

    }

    @Override
    public void transactionTypesChanged()
    {
        transactionTypeAdapter.notifyDataSetChanged();
        favoritesListAdapter.notifyDataSetChanged();
    }

    @Override
    public void transactionTypeAdded(CustomTransactionType transactionType)
    {
        transactionTypeAdapter.notifyDataSetChanged();

        int position = balancerEditor.getTransactionTypes().indexOf(transactionType);

        spinnerTransactionType.setSelection(position);

        updateSelectedTransactionType();
    }

    @Override
    public void transactionTypeEdited(CustomTransactionType transactionType)
    {
        transactionTypeAdapter.notifyDataSetChanged();

        updateSelectedTransactionType();
    }

    @Override
    public void transactionTypeRemoved(CustomTransactionType transactionType)
    {
        transactionTypeAdapter.notifyDataSetChanged();

        spinnerTransactionType.setSelection(0);

        updateSelectedTransactionType();
    }

    @Override
    public void transactionAdded()
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

        textViewTransactionAvailable = findViewById(R.id.textViewTransactionAvailable);
        spinnerTransactionType = findViewById(R.id.spinnerTransactionType);
        editTextAmount = findViewById(R.id.editTextAmount);

        favoritesList = findViewById(R.id.listFavorites);

        buttonNewTransactionType = findViewById(R.id.buttonNewTransactionType);
        buttonEditTransactionType = findViewById(R.id.buttonEditTransactionType);
        buttonRemoveTransactionType = findViewById(R.id.buttonRemoveTransactionType);

        buttonAddTransaction = findViewById(R.id.buttonAddCalories);

        buttonDone = findViewById(R.id.buttonDone);

        transactionTypeAdapter = new SimpleListAdapterBuilder<>(
            this,
            balancerEditor::getTransactionTypes,
            TransactionType::getName)
            .create();

        spinnerTransactionType.setAdapter(transactionTypeAdapter);
        spinnerTransactionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                updateSelectedTransactionType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        editTextAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        favoritesListAdapter = new SimpleListAdapterBuilder<>(
            this,
            () -> getFavoriteTransactionTypes(balancer),
            TransactionType::getName)
            .withSecondaryTextFunction(t -> Integer.toString(t.getAmount()))
            .create();
        favoritesList.setAdapter(favoritesListAdapter);
        favoritesList.setOnItemClickListener((adapterView, view, position, id) ->
        {
            TransactionType transactionType =
                (TransactionType)favoritesList.getItemAtPosition(position);

            DialogUtils.showConfirmationDialog(
                BalanceActivity.this,
                "Add transaction",
                transactionType.getName() + " (" + transactionType.getAmount() + " " + balancer.getUnit() + ")",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    addTransaction(transactionType);
                });
        });

        buttonNewTransactionType.setOnClickListener(v -> addTransactionType());
        buttonEditTransactionType.setOnClickListener(v -> editTransactionType());
        buttonRemoveTransactionType.setOnClickListener(v -> removeTransactionType());
        buttonAddTransaction.setOnClickListener(v -> addTransaction((TransactionType)spinnerTransactionType.getSelectedItem()));

        buttonDone.setOnClickListener(v -> finish());

        new EditTextButtonEnabledLink(
            buttonAddTransaction,
            new EditTextCriteria(editTextAmount, EditTextCriteria.IS_VALID_INT));

        setActivityEnabled(balancer.isEnabled());
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        balancerEditor.addListener(this);

        updateName();
        updateAvailable();
        updateHint();
        updateSelectedTransactionType();
    }

    private @NonNull List<TransactionType> getFavoriteTransactionTypes(Balancer balancer)
    {
        return balancer.getTransactionTypes()
            .stream()
            .filter(TransactionType::isFavorite)
            .collect(Collectors.toList());
    }

    private void addTransactionType()
    {
        showTransactionTypeDialog(null, null, null, (name, calories, isFavorite) ->
        {
            balancerEditor.addTransactionType(new CustomTransactionType(
                name,
                calories,
                isFavorite));
            serena.save();
        });
    }

    private void editTransactionType()
    {
        CustomTransactionType transactionType = (CustomTransactionType)spinnerTransactionType.getSelectedItem();

        assert transactionType != null;

        showTransactionTypeDialog(
            transactionType.getName(),
            transactionType.getAmount(),
            transactionType.isFavorite(),
            (name, amount, isFavorite) ->
            {
                balancerEditor.setTransactionTypeName(transactionType, name);
                balancerEditor.setTransactionTypeAmount(transactionType, amount);
                balancerEditor.setFavorite(transactionType, isFavorite);

                serena.save();
            });
    }

    private void removeTransactionType()
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Remove transaction type",
            "Are you sure you want to remove the transaction type?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                CustomTransactionType transactionType =
                    (CustomTransactionType)spinnerTransactionType.getSelectedItem();

                assert transactionType != null;

                balancerEditor.removeTransactionType(transactionType);

                serena.save();
            });
    }

    private void addTransaction(TransactionType transactionType)
    {
        int enteredTransactionAmount = transactionType.isQuick()
            ? Integer.parseInt(editTextAmount.getText().toString())
            : transactionType.getAmount();

        balancerEditor.addTransaction(transactionType.getName(), enteredTransactionAmount);

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
    private void updateSelectedTransactionType()
    {
        TransactionType selectedTransactionType = (TransactionType)spinnerTransactionType.getSelectedItem();

        assert selectedTransactionType != null;

        if (selectedTransactionType.isQuick())
        {
            editTextAmount.setText("");
            editTextAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        else
        {
            editTextAmount.setText(Integer.toString(selectedTransactionType.getAmount()));
            editTextAmount.setInputType(InputType.TYPE_NULL);
        }

        buttonEditTransactionType.setEnabled(!selectedTransactionType.isQuick());
        buttonRemoveTransactionType.setEnabled(!selectedTransactionType.isQuick());
    }

    private void updateName()
    {
        textViewName.setText(balancerEditor.getName());
    }

    private void updateAvailable()
    {
        if (!balancerEditor.isEnabled())
        {
            textViewTransactionAvailable.setText("-");
        }
        else
        {
            textViewTransactionAvailable.setText(
                String.format("%s %s", balancerEditor.getAvailable(), balancerEditor.getUnit()));
        }

    }

    private void updateHint()
    {
        String hint = balancerEditor.getUnit().isEmpty() ? "amount" : balancerEditor.getUnit();

        editTextAmount.setHint(hint);
    }

    private void setActivityEnabled(boolean isEnabled)
    {
        buttonNewTransactionType.setEnabled(isEnabled);
        buttonAddTransaction.setEnabled(isEnabled);
        spinnerTransactionType.setEnabled(isEnabled);
        favoritesList.setEnabled(isEnabled);

        if (isEnabled)
        {
            updateSelectedTransactionType();
        }
        else
        {
            buttonEditTransactionType.setEnabled(false);
            buttonRemoveTransactionType.setEnabled(false);
        }
    }

    private void showSettingsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_balancer_settings, null);
        builder.setView(dialogView);

        CheckBox checkBoxIsEnabled = dialogView.findViewById(R.id.checkBoxIsEnabled);
        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextUnit = dialogView.findViewById(R.id.editTextUnit);
        EditText editTextChangePerDay = dialogView.findViewById(R.id.editTextTransactionPerDay);
        EditText editTextMaxValue = dialogView.findViewById(R.id.editTextMaxValue);
        EditText editTextMinValue = dialogView.findViewById(R.id.editTextMinValue);
        CheckBox checkBoxAllowQuick = dialogView.findViewById(R.id.checkBoxAllowQuick);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        checkBoxIsEnabled.setChecked(balancerEditor.isEnabled());
        editTextName.setText(balancerEditor.getName());
        editTextUnit.setText(balancerEditor.getUnit());
        editTextChangePerDay.setText(String.valueOf(balancerEditor.getChangePerDay()));
        editTextMaxValue.setText(balancerEditor.hasMaxValue()
            ? Integer.toString(balancerEditor.getMaxValue())
            : "");
        editTextMinValue.setText(balancerEditor.hasMinValue()
            ? Integer.toString(balancerEditor.getMinValue())
            : "");
        checkBoxAllowQuick.setChecked(balancerEditor.isQuickAllowed());
        checkBoxAllowQuick.setEnabled(balancerEditor.isQuickDisableable());

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonOk.setOnClickListener(v ->
        {
            String changePerDay = editTextChangePerDay.getText().toString().trim();

            if (!changePerDay.isEmpty())
            {
                balancerEditor.setEnabled(checkBoxIsEnabled.isChecked());
                balancerEditor.setName(editTextName.getText().toString().trim());
                balancerEditor.setUnit(editTextUnit.getText().toString().trim());
                balancerEditor.setMaxValue(getIntegerValue(editTextMaxValue));
                balancerEditor.setMinValue(getIntegerValue(editTextMinValue));
                balancerEditor.setAllowQuick(checkBoxAllowQuick.isChecked());
                balancerEditor.setChangePerDay(Integer.parseInt(changePerDay));

                serena.save();

                dialog.dismiss();
            }
        });

        dialog.show();

        new EditTextButtonEnabledLink(
            buttonOk,
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextChangePerDay, EditTextCriteria.IS_VALID_INT),
            new EditTextCriteria(editTextMaxValue, et ->
            {
                if (et.getText().toString().isEmpty()) return true;

                Integer maxValue = getIntegerValue(et);

                return maxValue != null && maxValue >= 0;
            }),
            new EditTextCriteria(editTextMinValue, et ->
            {
                if (et.getText().toString().isEmpty()) return true;

                Integer minValue = getIntegerValue(et);

                return minValue != null && minValue <= 0;
            }));
    }

    private static @Nullable Integer getIntegerValue(EditText editText)
    {
        return EditTextCriteria.isValidInteger(editText)
            ? Integer.parseInt(editText.getText().toString())
            : null;
    }

    @SuppressLint("SetTextI18n")
    private void showTransactionTypeDialog(
        String initialName,
        Integer initialCalories,
        Boolean initialIsFavorite,
        TransactionTypeOkListener okClickListener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_transaction_type, null);
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
            String name = editTextName.getText().toString().trim();
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

    private interface TransactionTypeOkListener
    {
        void onOkClick(String name, int amount, boolean isFavorite);
    }
}