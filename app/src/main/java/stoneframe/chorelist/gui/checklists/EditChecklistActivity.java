package stoneframe.chorelist.gui.checklists;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.RecyclerAdapter;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.checklists.ChecklistItem;

public class EditChecklistActivity extends Activity
{
    public static final int DONE = 0;
    public static final int REMOVE = 1;

    private final ColorDrawable editBackground = new ColorDrawable(Color.BLUE);
    private final ColorDrawable deleteBackground = new ColorDrawable(Color.RED);

    private RecyclerAdapter<ChecklistItem> checklistItemsAdapter;

    private EditText checklistNameEditText;
    private RecyclerView checklistItemsListView;

    private Button removeButton;
    private Button buttonAddItem;
    private Button buttonDone;

    private Drawable editIcon;
    private Drawable deleteIcon;


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

        editIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_edit);

        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_delete);

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

        checklistItemsAdapter = new RecyclerAdapter<>(
            checklist::getItems,
            ChecklistItem::getDescription);
        checklistItemsListView.setLayoutManager(new LinearLayoutManager(this));
        checklistItemsListView.setAdapter(checklistItemsAdapter);
        checklistItemsListView.addItemDecoration(new DividerItemDecoration(
            getBaseContext(),
            LinearLayoutManager.VERTICAL));

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP | ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            private final int backgroundCornerOffset = 20;

            @Override
            public boolean onMove(
                @NonNull RecyclerView recyclerView,
                RecyclerView.ViewHolder viewHolder,
                RecyclerView.ViewHolder target)
            {
                int fromPosition = viewHolder.getBindingAdapterPosition();
                int toPosition = target.getBindingAdapterPosition();

                ChecklistItem item = checklist.getItems().get(fromPosition);

                checklist.moveItem(item, toPosition);

                checklistItemsAdapter.notifyItemMoved(fromPosition, toPosition);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                int position = viewHolder.getBindingAdapterPosition();
                ChecklistItem item = checklist.getItems().get(position);

                if (direction == ItemTouchHelper.LEFT)
                {
                    checklist.removeItem(item);
                    checklistItemsAdapter.notifyItemRemoved(position);
                }

                if (direction == ItemTouchHelper.RIGHT)
                {
                    showChecklistItemDialog(
                        item,
                        () -> checklistItemsAdapter.notifyItemChanged(position));

                    checklistItemsAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onChildDraw(
                @NonNull Canvas c,
                @NonNull RecyclerView recyclerView,
                @NonNull RecyclerView.ViewHolder viewHolder,
                float dX,
                float dY,
                int actionState,
                boolean isCurrentlyActive)
            {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive);

                if (isSwipingToTheLeft(dX))
                {
                    showDeleteIcon(c, (int)dX, viewHolder.itemView);
                }

                if (isSwipingToTheRight(dX))
                {
                    showEditIcon(c, (int)dX, viewHolder.itemView);
                }

                if (isNotSwiping(dX))
                {
                    clearIcons();
                }
            }

            private void showDeleteIcon(@NonNull Canvas c, int dX, View itemView)
            {
                int iconMargin = (itemView.getHeight() - deleteIcon.getIntrinsicHeight()) / 2;

                int iconLeft = itemView.getRight() - iconMargin - deleteIcon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;

                int backgroundLeft = itemView.getRight() + dX - backgroundCornerOffset;
                int backgroundRight = itemView.getRight();

                drawBackground(c, itemView, backgroundLeft, backgroundRight, deleteBackground);
                drawIcon(c, itemView, iconLeft, iconRight, deleteIcon);
            }

            private void showEditIcon(@NonNull Canvas c, int dX, View itemView)
            {
                int iconMargin = (itemView.getHeight() - editIcon.getIntrinsicHeight()) / 2;

                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = itemView.getLeft() + iconMargin + editIcon.getIntrinsicWidth();

                int backgroundLeft = itemView.getLeft();
                int backgroundRight = itemView.getLeft() + dX + backgroundCornerOffset;

                drawBackground(c, itemView, backgroundLeft, backgroundRight, editBackground);
                drawIcon(c, itemView, iconLeft, iconRight, editIcon);
            }

            private void drawIcon(
                @NonNull Canvas c,
                View itemView,
                int iconLeft,
                int iconRight,
                Drawable icon)
            {
                int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
                int iconBottom = iconTop + icon.getIntrinsicHeight();

                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                icon.draw(c);
            }

            private void drawBackground(
                @NonNull Canvas c,
                View itemView,
                int backgroundLeft,
                int backgroundRight,
                ColorDrawable background)
            {
                background.setBounds(
                    backgroundLeft,
                    itemView.getTop(),
                    backgroundRight,
                    itemView.getBottom());

                background.draw(c);
            }

            private void clearIcons()
            {
                deleteIcon.setBounds(0, 0, 0, 0);
                editIcon.setBounds(0, 0, 0, 0);
            }

            private boolean isSwipingToTheLeft(float dX)
            {
                return dX < 0;
            }

            private boolean isSwipingToTheRight(float dX)
            {
                return dX > 0;
            }

            private boolean isNotSwiping(float dX)
            {
                return dX == 0;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(checklistItemsListView);

        removeButton.setOnClickListener(v -> DialogUtils.showConfirmationDialog(
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
                checklistItemsAdapter.notifyItemInserted(checklist.getItems().size() - 1);
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
