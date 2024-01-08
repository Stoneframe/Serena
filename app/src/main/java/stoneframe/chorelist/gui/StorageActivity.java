package stoneframe.chorelist.gui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import stoneframe.chorelist.R;

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
}
