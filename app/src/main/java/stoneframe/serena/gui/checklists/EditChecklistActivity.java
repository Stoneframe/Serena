package stoneframe.serena.gui.checklists;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
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

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.enable.ButtonEnabledLink;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.gui.util.RecyclerAdapter;
import stoneframe.serena.gui.util.TextChangedListener;
import stoneframe.serena.checklists.Checklist;
import stoneframe.serena.checklists.ChecklistEditor;
import stoneframe.serena.checklists.ChecklistItem;

public class EditChecklistActivity extends EditActivity implements ChecklistEditor.ChecklistEditorListener
{
    private final ColorDrawable editBackground = new ColorDrawable(Color.parseColor("#AECCE4"));
    private final ColorDrawable deleteBackground = new ColorDrawable(Color.parseColor("#FF8164"));

    private RecyclerAdapter<ChecklistItem> checklistItemsAdapter;

    private EditText checklistNameEditText;
    private RecyclerView checklistItemsListView;

    private Button addItemButton;

    private Drawable editIcon;
    private Drawable deleteIcon;

    private ChecklistEditor checklistEditor;

    @Override
    public void nameChanged()
    {

    }

    @Override
    public void checklistItemAdded(int position, ChecklistItem item)
    {
        checklistItemsAdapter.notifyItemInserted(checklistEditor.getItems().size() - 1);
    }

    @Override
    public void checklistItemRemoved(int position, ChecklistItem item)
    {
        checklistItemsAdapter.notifyItemRemoved(position);
    }

    @Override
    public void checklistItemMoved(int oldPosition, int newPosition, ChecklistItem item)
    {
        checklistItemsAdapter.notifyItemMoved(oldPosition, newPosition);
    }

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
        Checklist checklist = globalState.getActiveChecklist();

        checklistEditor = serena.getChecklistManager().getChecklistEditor(checklist);

        checklistNameEditText = findViewById(R.id.nameEditText);
        checklistItemsListView = findViewById(R.id.listView);

        addItemButton = findViewById(R.id.addItemButton);

        editIcon = ContextCompat.getDrawable(this, R.drawable.ic_edit);
        deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete);

        checklistNameEditText.setText(checklist.getName());
        checklistNameEditText.addTextChangedListener(
            new TextChangedListener(str -> checklistEditor.setName(str)));

        checklistItemsAdapter = new RecyclerAdapter<>(
            checklistEditor::getItems,
            ChecklistItem::getDescription);
        checklistItemsListView.setLayoutManager(new LinearLayoutManager(this));
        checklistItemsListView.setAdapter(checklistItemsAdapter);
        checklistItemsListView.addItemDecoration(new DividerItemDecoration(
            getBaseContext(),
            LinearLayoutManager.VERTICAL));

        addItemButton.setOnClickListener(v ->
        {
            ChecklistItem item = new ChecklistItem("");

            showChecklistItemDialog(item, () -> checklistEditor.addItem(item));
        });

        setupSwipeMechanics();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        checklistEditor.addListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        checklistEditor.removeListener(this);
    }

    @Override
    protected EnableCriteria[] getSaveEnabledCriteria()
    {
        return new EditTextCriteria[]
            {
                new EditTextCriteria(checklistNameEditText, EditTextCriteria.IS_NOT_EMPTY),
            };
    }

    @Override
    protected boolean onCancel()
    {
        checklistEditor.revert();

        return true;
    }

    @Override
    protected boolean onSave(int action)
    {
        checklistEditor.save();
        serena.save();

        return true;
    }

    @Override
    protected void onRemove()
    {
        checklistEditor.remove();
        serena.save();
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

        new ButtonEnabledLink(
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

    private void setupSwipeMechanics()
    {
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

                ChecklistItem item = checklistEditor.getItems().get(fromPosition);

                checklistEditor.moveItem(item, toPosition);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
                int position = viewHolder.getBindingAdapterPosition();
                ChecklistItem item = checklistEditor.getItems().get(position);

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
                                checklistEditor.removeItem(item);
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
}
