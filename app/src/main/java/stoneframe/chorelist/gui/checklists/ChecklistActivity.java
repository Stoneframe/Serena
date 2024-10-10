package stoneframe.chorelist.gui.checklists;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.SimpleCheckboxListAdapter;
import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.checklists.ChecklistItem;

public class ChecklistActivity extends AppCompatActivity
{
    private ActivityResultLauncher<Intent> editChecklistLauncher;

    private SimpleCheckboxListAdapter<ChecklistItem> checklistItemAdapter;

    private TextView checklistNameTextView;

    private Button editButton;
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
        ListView checkListItemView = findViewById(R.id.checklistItemView);
        checkListItemView.setAdapter(checklistItemAdapter);
        checkListItemView.setOnItemClickListener((parent, view, position, id) ->
        {
            ChecklistItem item = (ChecklistItem)checklistItemAdapter.getItem(position);

            if (checklistItemAdapter.isChecked(item))
            {
                checklistItemAdapter.setUnchecked(item);
            }
            else
            {
                checklistItemAdapter.setChecked(item);
            }
        });

        editButton = findViewById(R.id.editChecklistButton);
        doneButton = findViewById(R.id.doneButton);

        editButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, EditChecklistActivity.class);
            editChecklistLauncher.launch(intent);
        });

        doneButton.setOnClickListener(v -> finish());
    }

    private void editChecklistCallback(ActivityResult result)
    {
        if (result.getResultCode() == EditChecklistActivity.REMOVE)
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
