package stoneframe.chorelist.gui.checklists;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.EditActivity;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.EditTextButtonEnabledLink;
import stoneframe.chorelist.gui.util.EditTextCriteria;
import stoneframe.chorelist.gui.util.RecyclerAdapter;
import stoneframe.chorelist.gui.util.TextChangedListener;
import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.checklists.ChecklistItem;

public class EditChecklistActivity extends EditActivity
{
    private final ColorDrawable editBackground = new ColorDrawable(Color.parseColor("#AECCE4"));
    private final ColorDrawable deleteBackground = new ColorDrawable(Color.parseColor("#FF8164"));

    private RecyclerAdapter<ChecklistItem> checklistItemsAdapter;

    private EditText checklistNameEditText;
    private RecyclerView checklistItemsListView;

    private Button addItemButton;

    private Drawable editIcon;
    private Drawable deleteIcon;

    private Checklist checklist;

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_edit_checklist;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Edit Checklist";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Checklist";
    }

    @Override
    protected void createActivity()
    {
        checklist = globalState.getActiveChecklist();
        checklist.edit();

        checklistNameEditText = findViewById(R.id.nameEditText);
        checklistItemsListView = findViewById(R.id.listView);

        addItemButton = findViewById(R.id.addItemButton);

        editIcon = ContextCompat.getDrawable(this, R.drawable.ic_edit);
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete);

        checklistNameEditText.setText(checklist.getName());
        checklistNameEditText.addTextChangedListener(
            new TextChangedListener(str -> checklist.setName(str)));

        checklistItemsAdapter = new RecyclerAdapter<>(
            checklist::getItems,
            ChecklistItem::getDescription);
        checklistItemsListView.setLayoutManager(new LinearLayoutManager(this));
        checklistItemsListView.setAdapter(checklistItemsAdapter);
        checklistItemsListView.addItemDecoration(new DividerItemDecoration(
            getBaseContext(),
            LinearLayoutManager.VERTICAL));

        addItemButton.setOnClickListener(v ->
        {
            ChecklistItem item = new ChecklistItem("");

            showChecklistItemDialog(item, () ->
            {
                checklist.addItem(item);
                checklistItemsAdapter.notifyItemInserted(checklist.getItems().size() - 1);
            });
        });

        new EditTextButtonEnabledLink(
            saveButton,
            new EditTextCriteria(checklistNameEditText, EditTextCriteria.IS_NOT_EMPTY));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                onCancel();

                setResult(RESULT_CANCEL);
                finish();
            }
        });

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
                    DialogUtils.showConfirmationDialog(
                        EditChecklistActivity.this,
                        "Remove checklist item",
                        "Are you sure you want to remove this checklist item?",
                        isConfirmed ->
                        {
                            if (isConfirmed)
                            {
                                checklist.removeItem(item);
                                checklistItemsAdapter.notifyItemRemoved(position);
                            }
                            else
                            {
                                checklistItemsAdapter.notifyItemChanged(position);
                            }
                        });
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
    }

    @Override
    protected void onCancel()
    {
        checklist.revert();
    }

    @Override
    protected void onSave(int action)
    {
        checklist.save();
        checklist.save();
    }

    @Override
    protected void onRemove()
    {
        choreList.removeChecklist(checklist);
        checklist.save();
    }

    private void showChecklistItemDialog(ChecklistItem checklistItem, Runnable onOk)
    {
        final EditText checklistItemDescriptionText = new EditText(this);

        checklistItemDescriptionText.setText(checklistItem.getDescription());
        checklistItemDescriptionText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        AlertDialog.Builder builder = getBuilder(
            checklistItem,
            onOk,
            checklistItemDescriptionText);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button okButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        new EditTextButtonEnabledLink(
            okButton,
            new EditTextCriteria(checklistItemDescriptionText, EditTextCriteria.IS_NOT_EMPTY));
    }

    @NonNull
    private AlertDialog.Builder getBuilder(
        ChecklistItem checklistItem,
        Runnable onOk,
        EditText checklistItemDescriptionText)
    {
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

        return builder;
    }
}
