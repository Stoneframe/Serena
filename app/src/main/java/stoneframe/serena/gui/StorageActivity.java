package stoneframe.serena.gui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.serena.R;
import stoneframe.serena.gui.util.DialogUtils;

public class StorageActivity extends AppCompatActivity
{
    private EditText storageText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);

        Intent intent = getIntent();

        storageText = findViewById(R.id.storageText);
        storageText.setText(intent.getStringExtra("Storage"));
    }

    public void onSaveClick(View view)
    {
        Intent intent = new Intent();

        intent.putExtra("Storage", storageText.getText().toString());

        setResult(RESULT_OK, intent);
        finish();
    }

    public void onPasteClick(View view)
    {
        DialogUtils.showConfirmationDialog(
            this,
            "Paste",
            "Are you sure you want to paste? It will overwrite the current content.",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

                if (!clipboard.hasPrimaryClip())
                {
                    return;
                }

                ClipData clipData = clipboard.getPrimaryClip();

                if (clipData != null && clipData.getItemCount() > 0)
                {
                    ClipData.Item item = clipData.getItemAt(0);

                    storageText.setText(item.getText().toString());
                }
            }
        );
    }

    public void onCopyClick(View view)
    {
        ClipboardManager clipboard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newPlainText(
            "Serena Storage",
            storageText.getText().toString());

        clipboard.setPrimaryClip(clip);
    }
}
