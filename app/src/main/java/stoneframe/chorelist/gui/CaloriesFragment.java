package stoneframe.chorelist.gui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;

public class CaloriesFragment extends Fragment
{
    private TextView textViewCaloriesValue;
    private EditText editTextCalories;
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
        editTextCalories = view.findViewById(R.id.editTextCalories);
        buttonAddCalories = view.findViewById(R.id.buttonAddCalories);
        buttonSettings = view.findViewById(R.id.buttonSettings);

        updateAvailableCalories();

        buttonAddCalories.setOnClickListener(v ->
        {
            int enteredCalories = Integer.parseInt(editTextCalories.getText().toString());

            choreList.addCalorieConsumption("Quick", enteredCalories);
            choreList.save();

            editTextCalories.setText("");

            updateAvailableCalories();
        });

        buttonSettings.setOnClickListener(v -> showSettingsDialog());

        new EditTextButtonEnabledLink(
            buttonAddCalories,
            new EditTextCriteria(editTextCalories, EditTextCriteria.IS_VALID_INT));

        return view;
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
}