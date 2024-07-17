package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.util.BiConsumer;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.calories.CalorieConsumptionType;
import stoneframe.chorelist.model.calories.CustomCalorieConsumptionType;

public class CaloriesFragment extends Fragment
{
    private TextView textViewCaloriesValue;
    private Spinner spinnerConsumptionType;
    private EditText editTextCalories;
    private Button buttonNewConsumptionType;
    private Button buttonEditConsumptionType;
    private Button buttonRemoveConsumptionType;
    private Button buttonAddCalories;
    private Button buttonSettings;

    private ChoreList choreList;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        choreList = GlobalState.getInstance().getChoreList();

        View view = inflater.inflate(R.layout.fragment_calories, container, false);

        textViewCaloriesValue = view.findViewById(R.id.textViewCaloriesValue);
        spinnerConsumptionType = view.findViewById(R.id.spinnerConsumptionType);
        editTextCalories = view.findViewById(R.id.editTextCalories);

        buttonNewConsumptionType = view.findViewById(R.id.buttonNewConsumptionType);
        buttonEditConsumptionType = view.findViewById(R.id.buttonEditConsumptionType);
        buttonRemoveConsumptionType = view.findViewById(R.id.buttonRemoveConsumptionType);

        buttonAddCalories = view.findViewById(R.id.buttonAddCalories);
        buttonSettings = view.findViewById(R.id.buttonSettings);

        updateAvailableCalories();

        SimpleListAdapter<CalorieConsumptionType> consumptionTypeAdapter = new SimpleListAdapter<>(
            requireContext(),
            choreList::getCaloriesConsumptionTypes,
            CalorieConsumptionType::getName);

        spinnerConsumptionType.setAdapter(consumptionTypeAdapter);
        spinnerConsumptionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                updateSelectedConsumptionType();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        buttonNewConsumptionType.setOnClickListener(v ->
            showConsumptionTypeDialog(null, null, (name, calories) ->
            {
                choreList.addCalorieConsumptionType(name, calories);
                choreList.save();
            }));

        buttonEditConsumptionType.setOnClickListener(v ->
        {
            CustomCalorieConsumptionType consumptionType = (CustomCalorieConsumptionType)spinnerConsumptionType.getSelectedItem();

            assert consumptionType != null;

            showConsumptionTypeDialog(
                consumptionType.getName(),
                consumptionType.getCalories(),
                (name, calories) ->
                {
                    consumptionType.setName(name);
                    consumptionType.setCalories(calories);

                    choreList.save();

                    updateSelectedConsumptionType();

                    consumptionTypeAdapter.notifyDataSetChanged();
                });
        });

        buttonRemoveConsumptionType.setOnClickListener(v ->
            DialogUtils.showConfirmationDialog(
                requireContext(),
                "Remove consumption type",
                "Are you sure you want to remove the consumption type?",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    CustomCalorieConsumptionType consumptionType =
                        (CustomCalorieConsumptionType)spinnerConsumptionType.getSelectedItem();

                    assert consumptionType != null;

                    choreList.removeCalorieConsumptionType(consumptionType);

                    choreList.save();

                    spinnerConsumptionType.setSelection(0);
                }));

        buttonAddCalories.setOnClickListener(v ->
        {
            CalorieConsumptionType consumptionType = (CalorieConsumptionType)spinnerConsumptionType.getSelectedItem();

            int enteredCalories = consumptionType.isQuick()
                ? Integer.parseInt(editTextCalories.getText().toString())
                : consumptionType.getCalories();

            choreList.addCalorieConsumption(consumptionType.getName(), enteredCalories);
            choreList.save();

            if (consumptionType.isQuick())
            {
                editTextCalories.setText("");
            }

            updateAvailableCalories();
        });

        buttonSettings.setOnClickListener(v -> showSettingsDialog());

        new EditTextButtonEnabledLink(
            buttonAddCalories,
            new EditTextCriteria(editTextCalories, EditTextCriteria.IS_VALID_INT));

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void updateSelectedConsumptionType()
    {
        CalorieConsumptionType selectedConsumptionType = (CalorieConsumptionType)spinnerConsumptionType.getSelectedItem();

        assert selectedConsumptionType != null;

        if (!selectedConsumptionType.isQuick())
        {
            editTextCalories.setText(Integer.toString(selectedConsumptionType.getCalories()));
        }
        else
        {
            editTextCalories.setText("");
        }

        editTextCalories.setEnabled(selectedConsumptionType.isQuick());
        buttonEditConsumptionType.setEnabled(!selectedConsumptionType.isQuick());
        buttonRemoveConsumptionType.setEnabled(!selectedConsumptionType.isQuick());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        InputMethodManager imm = (InputMethodManager)requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
    }

    private void updateAvailableCalories()
    {
        textViewCaloriesValue.setText(String.valueOf(choreList.getAvailableCalories()));
    }

    private void showSettingsDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_calories_settings, null);
        builder.setView(dialogView);

        EditText editTextCaloriesPerDay = dialogView.findViewById(R.id.editTextCaloriesPerDay);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonOk = dialogView.findViewById(R.id.buttonOk);

        editTextCaloriesPerDay.setText(String.valueOf(choreList.getCalorieIncrementPerDay()));

        AlertDialog dialog = builder.create();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonOk.setOnClickListener(v ->
        {
            String caloriesPerDay = editTextCaloriesPerDay.getText().toString();

            if (!caloriesPerDay.isEmpty())
            {
                choreList.setCalorieIncrementPerDay(Integer.parseInt(caloriesPerDay));
                choreList.save();

                dialog.dismiss();

                updateAvailableCalories();
            }
        });

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void showConsumptionTypeDialog(
        String initialName,
        Integer initialCalories,
        BiConsumer<String, Integer> okClickListener)
    {
        final EditText editTextName = new EditText(requireContext());
        editTextName.setHint("Name");
        editTextName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        final EditText editTextCalories = new EditText(requireContext());
        editTextCalories.setHint("Calories");
        editTextCalories.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        if (initialName != null) editTextName.setText(initialName);
        if (initialCalories != null) editTextCalories.setText(initialCalories.toString());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(16, 16, 16, 16);
        layout.addView(editTextName);
        layout.addView(editTextCalories);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
            .setTitle("Consumption Type")
            .setView(layout)
            .setPositiveButton("OK", (d, which) ->
            {
                String name = editTextName.getText().toString();
                int calories = Integer.parseInt(editTextCalories.getText().toString());

                okClickListener.accept(name, calories);
            })
            .setNegativeButton("Cancel", null)
            .create();

        dialog.show();

        new EditTextButtonEnabledLink(
            dialog.getButton(AlertDialog.BUTTON_POSITIVE),
            new EditTextCriteria(editTextName, EditTextCriteria.IS_NOT_EMPTY),
            new EditTextCriteria(editTextCalories, EditTextCriteria.IS_NOT_EMPTY));
    }
}