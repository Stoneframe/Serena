package stoneframe.chorelist.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.Checklist;
import stoneframe.chorelist.model.ChecklistItem;

public class EditChecklistActivity extends Activity
{
    public static final int DONE = 0;
    public static final int REMOVE = 1;

    private SimpleListAdapter<ChecklistItem> checklistItemsAdapter;

    private EditText checklistNameEditText;
    private ListView checklistItemsListView;

    private Button removeButton;
    private Button buttonAddItem;
    private Button buttonDone;

    private ChoreList choreList;
    private Checklist checklist;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_checklist);

        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();
        checklist = globalState.getActiveChecklist();

        checklistNameEditText = findViewById(R.id.editText);
        checklistItemsListView = findViewById(R.id.listView);

        removeButton = findViewById(R.id.buttonRemove);
        buttonAddItem = findViewById(R.id.buttonAddItem);
        buttonDone = findViewById(R.id.buttonDone);

        checklistNameEditText.setText(checklist.getName());
        checklistNameEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                checklist.setName(checklistNameEditText.getText().toString());
            }
        });

        checklistItemsAdapter = new SimpleListAdapter<>(
            this,
            checklist::getItems,
            ChecklistItem::getDescription);
        checklistItemsListView.setAdapter(checklistItemsAdapter);
        checklistItemsListView.setOnItemClickListener((parent, view, position, id) ->
        {
            ChecklistItem item = (ChecklistItem)checklistItemsAdapter.getItem(position);

            showChecklistItemDialog(item, () ->
            {
            });
        });
        checklistItemsListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            ChecklistItem item = (ChecklistItem)checklistItemsAdapter.getItem(position);

            checklist.removeItem(item);
            checklistItemsAdapter.notifyDataSetChanged();

            return true;
        });

        removeButton.setOnClickListener(v ->
            DialogUtils.showConfirmationDialog(
                this,
                "Remove Checklist",
                "Are you sure you want to remove the checklist?",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    choreList.removeChecklist(checklist);
                    setResult(REMOVE);
                    finish();
                }));

        buttonAddItem.setOnClickListener(v ->
        {
            ChecklistItem item = new ChecklistItem("");

            showChecklistItemDialog(item, () ->
            {
                checklist.addItem(item);
                checklistItemsAdapter.notifyDataSetChanged();
            });
        });

        buttonDone.setOnClickListener(v ->
        {
            setResult(DONE);
            finish();
        });

        new EditTextButtonEnabledLink(
            buttonDone,
            new EditTextCriteria(checklistNameEditText, EditTextCriteria.IS_NOT_EMPTY));
    }

    private void showChecklistItemDialog(ChecklistItem checklistItem, Runnable onOk)
    {
        final EditText checklistItemDescriptionText = new EditText(this);

        checklistItemDescriptionText.setText(checklistItem.getDescription());
        checklistItemDescriptionText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Checklist item");
        builder.setView(checklistItemDescriptionText);

        builder.setPositiveButton("OK", (dialog, which) ->
        {
            String checklistItemDescription = checklistItemDescriptionText.getText()
                .toString()
                .trim();

            checklistItem.setDescription(checklistItemDescription);

            onOk.run();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        new EditTextButtonEnabledLink(
            okButton,
            new EditTextCriteria(checklistItemDescriptionText, EditTextCriteria.IS_NOT_EMPTY));
    }
}
