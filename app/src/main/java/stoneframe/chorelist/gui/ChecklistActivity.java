package stoneframe.chorelist.gui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Checklist;
import stoneframe.chorelist.model.ChecklistItem;

public class ChecklistActivity extends AppCompatActivity
{
    private SimpleCheckboxListAdapter<ChecklistItem> checklistItemAdapter;

    private TextView checklistNameTextView;
    
    private Button removeButton;
    private Button editButton;
    private Button doneButton;

    private ChoreList choreList;
    private Checklist checklist;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checklist);

        GlobalState globalState = GlobalState.getInstance(this);

        choreList = globalState.getChoreList();
        checklist = globalState.ActiveChecklist;

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

        removeButton = findViewById(R.id.buttonRemove);
        editButton = findViewById(R.id.buttonEditChecklist);
        doneButton = findViewById(R.id.buttonDone);

        removeButton.setOnClickListener(v ->
        {
            choreList.removeChecklist(checklist);
            finish();
        });

        editButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(this, EditChecklistActivity.class);
            startActivity(intent);
        });

        doneButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        checklistNameTextView.setText(checklist.getName());
        checklistItemAdapter.notifyDataSetChanged();
    }
}
