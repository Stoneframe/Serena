package stoneframe.serena.gui.checklists;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleCheckboxListAdapter;
import stoneframe.serena.model.checklists.Checklist;
import stoneframe.serena.model.checklists.ChecklistItem;

public class ChecklistActivity extends AppCompatActivity
{
    private ActivityResultLauncher<Intent> editChecklistLauncher;

    private SimpleCheckboxListAdapter<ChecklistItem> checklistItemAdapter;

    private TextView checklistNameTextView;

    private Button editButton;
    private Button resetButton;
    private Button doneButton;

    private Checklist checklist;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        setTitle("Checklist");

        GlobalState globalState = GlobalState.getInstance();

        checklist = globalState.getActiveChecklist();

        editChecklistLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editChecklistCallback);

        checklistNameTextView = findViewById(R.id.checklistNameTextView);
        checklistNameTextView.setText(checklist.getName());

        checklistItemAdapter = new SimpleCheckboxListAdapter<>(
            getBaseContext(),
            checklist::getItems,
            ChecklistItem::getDescription);

        checklist.getItems()
            .stream()
            .filter(ChecklistItem::isChecked)
            .forEach(item -> checklistItemAdapter.setChecked(item));

        ListView checkListItemView = findViewById(R.id.checklistItemView);
        checkListItemView.setAdapter(checklistItemAdapter);
        checkListItemView.setOnItemClickListener((parent, view, position, id) ->
        {
            ChecklistItem item = (ChecklistItem)checklistItemAdapter.getItem(position);

            if (checklistItemAdapter.isChecked(item))
            {
                checklistItemAdapter.setUnchecked(item);
                item.setChecked(false);
            }
            else
            {
                checklistItemAdapter.setChecked(item);
                item.setChecked(true);
            }
        });

        editButton = findViewById(R.id.editChecklistButton);
        resetButton = findViewById(R.id.resetButton);
        doneButton = findViewById(R.id.doneButton);

        editButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, EditChecklistActivity.class)
                .putExtra("ACTION", EditChecklistActivity.ACTION_EDIT);

            editChecklistLauncher.launch(intent);
        });

        resetButton.setOnClickListener(v ->
        {
            checklist.getItems()
                .stream()
                .filter(ChecklistItem::isChecked)
                .forEach(item -> item.setChecked(false));

            checklistItemAdapter.resetChecked();
        });

        doneButton.setOnClickListener(v -> finish());
    }

    private void editChecklistCallback(ActivityResult result)
    {
        if (result.getResultCode() == EditChecklistActivity.RESULT_REMOVE)
        {
            finish();
        }
        else
        {
            checklistNameTextView.setText(checklist.getName());
            checklistItemAdapter.notifyDataSetChanged();
        }
    }
}
