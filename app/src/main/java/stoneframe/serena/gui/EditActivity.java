package stoneframe.serena.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import stoneframe.serena.R;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.ButtonEnabledLink;
import stoneframe.serena.gui.util.EnableCriteria;
import stoneframe.serena.model.Serena;

public abstract class EditActivity extends AppCompatActivity
{
    public static final int ACTION_ADD = 0;
    public static final int ACTION_EDIT = 1;

    public static final int RESULT_SAVE = 2;
    public static final int RESULT_CANCEL = 3;
    public static final int RESULT_REMOVE = 4;

    protected int action;

    protected Button cancelButton;
    protected Button saveButton;

    protected GlobalState globalState;
    protected Serena serena;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuItem removeItem = menu.findItem(R.id.action_remove);

        if (removeItem != null)
        {
            removeItem.setVisible(action == ACTION_EDIT);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.action_remove)
        {
            removeClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(getActivityLayoutId());
        setTitle(getActivityTitle());

        Intent intent = getIntent();

        action = intent.getIntExtra("ACTION", -1);

        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);

        cancelButton.setOnClickListener(v -> cancelClick());
        saveButton.setOnClickListener(v -> saveClick());

        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                cancelClick();
            }
        });

        createActivity();

        new ButtonEnabledLink(saveButton, getSaveEnabledCriteria());
    }

    protected abstract int getActivityLayoutId();

    protected abstract String getActivityTitle();

    protected abstract String getEditedObjectName();

    protected abstract void createActivity();

    protected abstract EnableCriteria[] getSaveEnabledCriteria();

    protected abstract void onCancel();

    protected abstract void onSave(int action);

    protected abstract void onRemove();

    private void cancelClick()
    {
        onCancel();

        setResult(RESULT_CANCEL);
        finish();
    }

    private void saveClick()
    {
        onSave(action);

        setResult(RESULT_SAVE);
        finish();
    }

    private void removeClick()
    {
        String name = getEditedObjectName();

        DialogUtils.showConfirmationDialog(
            this,
            "Remove " + name,
            "Are you sure you want to remove the " + name + "?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                onRemove();

                setResult(RESULT_REMOVE);
                finish();
            });
    }
}
