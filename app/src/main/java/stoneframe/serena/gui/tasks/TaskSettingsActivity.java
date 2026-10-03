package stoneframe.serena.gui.tasks;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.enable.ButtonEnabledLink;
import stoneframe.serena.gui.util.enable.CheckboxCriteria;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.OrEnableCriteria;
import stoneframe.serena.tasks.TaskManager;
import stoneframe.serena.tasks.TrustedSmsSender;

public class TaskSettingsActivity extends AppCompatActivity
{
    private final List<TrustedSmsSender> trustedSmsSenders = new ArrayList<>();

    private CheckBox limitTasksCheckBox;
    private EditText maximumTasksEditText;
    private TextView maximumTasksTextView;

    private CheckBox smsTaskCreationCheckBox;
    private TextView smsPermissionWarningTextView;
    private RecyclerView trustedSmsSendersList;

    private Button cancelButton;
    private Button saveButton;

    private ActivityResultLauncher<Intent> contactPickerLauncher;
    private ActivityResultLauncher<String> smsPermissionLauncher;
    private ActivityResultLauncher<String> notificationPermissionLauncher;
    private TrustedSmsSenderAdapter trustedSmsSenderAdapter;

    private boolean isShowingSmsPermissionWarning;
    private String smsPermissionWarningMessage;

    private Serena serena;
    private TaskManager taskManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_settings);

        serena = GlobalState.getInstance().getSerena();
        taskManager = serena.getTaskManager();

        registerActivityResultLaunchers();
        bindViews();
        loadDraft();
        updateMaximumTasksControls();
        setupTrustedSmsSendersList();
        updateSmsPermissionWarning();

        smsTaskCreationCheckBox.setOnCheckedChangeListener(
            (buttonView, isChecked) -> onSmsTaskCreationCheckedChanged(isChecked));

        limitTasksCheckBox.setOnClickListener(v -> updateMaximumTasksControls());
        findViewById(R.id.addTrustedSmsSenderButton).setOnClickListener(v -> pickTrustedSmsSender());
        cancelButton.setOnClickListener(v -> onCancelClick());
        saveButton.setOnClickListener(v -> onSaveClick());

        new ButtonEnabledLink(
            saveButton,
            new OrEnableCriteria(
                new CheckboxCriteria(limitTasksCheckBox, CheckboxCriteria.IS_NOT_CHECKED),
                new EditTextCriteria(maximumTasksEditText, TaskSettingsActivity::isValidMaximumNumberOfTasks)));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (smsTaskCreationCheckBox == null || !smsTaskCreationCheckBox.isChecked())
        {
            return;
        }

        if (!hasSmsPermission())
        {
            showSmsPermissionWarning(R.string.task_sms_permission_unavailable);
        }
        else if (getString(R.string.task_sms_permission_unavailable).equals(smsPermissionWarningMessage))
        {
            clearSmsPermissionWarning();
        }
    }

    private void registerActivityResultLaunchers()
    {
        contactPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::onContactPickerResult);

        smsPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onSmsPermissionResult);

        notificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::onNotificationPermissionResult);
    }

    private void bindViews()
    {
        limitTasksCheckBox = findViewById(R.id.limitTasksCheckBox);
        maximumTasksEditText = findViewById(R.id.maximumTasksEditText);
        maximumTasksTextView = findViewById(R.id.maximumTasksTextView);
        smsTaskCreationCheckBox = findViewById(R.id.smsTaskCreationCheckBox);
        smsPermissionWarningTextView = findViewById(R.id.smsPermissionWarningTextView);
        trustedSmsSendersList = findViewById(R.id.trustedSmsSendersList);
        cancelButton = findViewById(R.id.cancelButton);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadDraft()
    {
        Integer maximumNumberOfTasks = taskManager.getMaximumNumberOfTasksPerDay();
        boolean isLimited = maximumNumberOfTasks != null;

        limitTasksCheckBox.setChecked(isLimited);

        if (isLimited)
        {
            maximumTasksEditText.setText(String.format(Locale.getDefault(), "%d", maximumNumberOfTasks));
        }

        boolean smsEnabled = taskManager.isSmsTaskCreationEnabled();
        smsTaskCreationCheckBox.setChecked(smsEnabled);
        trustedSmsSenders.addAll(taskManager.getTrustedSmsSenders());

        if (smsEnabled && !hasSmsPermission())
        {
            showSmsPermissionWarning(R.string.task_sms_permission_unavailable);
        }
    }

    private void onSmsTaskCreationCheckedChanged(boolean isChecked)
    {
        if (!isChecked)
        {
            clearSmsPermissionWarning();
            return;
        }

        clearSmsPermissionWarning();

        if (!hasSmsPermission())
        {
            smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS);
            return;
        }

        requestNotificationPermissionIfNeeded();
    }

    private void onSmsPermissionResult(Boolean isGranted)
    {
        if (!smsTaskCreationCheckBox.isChecked())
        {
            return;
        }

        if (Boolean.TRUE.equals(isGranted))
        {
            clearSmsPermissionWarning();
            requestNotificationPermissionIfNeeded();
            return;
        }

        smsTaskCreationCheckBox.setChecked(false);
        showSmsPermissionWarning(R.string.task_sms_permission_denied);
    }

    private void requestNotificationPermissionIfNeeded()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && smsTaskCreationCheckBox.isChecked() && hasSmsPermission()
            && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
        {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void onNotificationPermissionResult(Boolean isGranted)
    {
        if (!smsTaskCreationCheckBox.isChecked() || Boolean.TRUE.equals(isGranted))
        {
            return;
        }

        Toast.makeText(this, R.string.task_sms_notification_permission_denied, Toast.LENGTH_LONG).show();
    }

    private void pickTrustedSmsSender()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(Phone.CONTENT_TYPE);

        try
        {
            contactPickerLauncher.launch(intent);
        }
        catch (ActivityNotFoundException e)
        {
            showSmsPermissionWarning(R.string.task_sms_no_contacts_app);
        }
    }

    private void onContactPickerResult(ActivityResult result)
    {
        if (result.getResultCode() != RESULT_OK || result.getData() == null)
        {
            return;
        }

        Uri contactUri = result.getData().getData();

        if (contactUri == null)
        {
            return;
        }

        addTrustedSmsSender(contactUri);
    }

    private void addTrustedSmsSender(Uri contactUri)
    {
        String[] projection = {Phone.NUMBER, Phone.DISPLAY_NAME};

        try (Cursor cursor = getContentResolver().query(contactUri, projection, null, null, null))
        {
            if (cursor == null || !cursor.moveToFirst())
            {
                showSmsPermissionWarning(R.string.task_sms_phone_number_unreadable);
                return;
            }

            String phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(Phone.NUMBER));
            String contactName = cursor.getString(cursor.getColumnIndexOrThrow(Phone.DISPLAY_NAME));

            if (areInputsValid(phoneNumber, contactName))
            {
                showSmsPermissionWarning(R.string.task_sms_contact_needs_name_and_number);
                return;
            }

            addTrustedSmsSender(phoneNumber.trim(), contactName.trim());
        }
        catch (SecurityException | IllegalArgumentException e)
        {
            showSmsPermissionWarning(R.string.task_sms_phone_number_unreadable);
        }
    }

    private static boolean areInputsValid(String phoneNumber, String contactName)
    {
        return phoneNumber == null || phoneNumber.trim().isEmpty()
            || contactName == null || contactName.trim().isEmpty();
    }

    private void addTrustedSmsSender(String phoneNumber, String contactName)
    {
        boolean alreadyTrusted = trustedSmsSenders.stream()
            .anyMatch(sender -> sender.getPhoneNumber().equals(phoneNumber));

        if (alreadyTrusted)
        {
            showSmsPermissionWarning(R.string.task_sms_number_already_trusted);
            return;
        }

        trustedSmsSenders.add(new TrustedSmsSender(phoneNumber, contactName));
        trustedSmsSenderAdapter.notifyItemInserted(trustedSmsSenders.size() - 1);
        clearSmsPermissionWarning();
    }

    private void setupTrustedSmsSendersList()
    {
        trustedSmsSenderAdapter = new TrustedSmsSenderAdapter(trustedSmsSenders, this::removeTrustedSmsSender);
        trustedSmsSendersList.setLayoutManager(new LinearLayoutManager(this));
        trustedSmsSendersList.setAdapter(trustedSmsSenderAdapter);
        trustedSmsSendersList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }

    private void removeTrustedSmsSender(TrustedSmsSender sender)
    {
        int position = trustedSmsSenders.indexOf(sender);

        if (position >= 0)
        {
            trustedSmsSenders.remove(position);
            trustedSmsSenderAdapter.notifyItemRemoved(position);
        }
    }

    private void onSaveClick()
    {
        Integer maximumNumberOfTasks = limitTasksCheckBox.isChecked()
            ? parseMaximumNumberOfTasks()
            : null;

        taskManager.setMaximumNumberOfTasksPerDay(maximumNumberOfTasks);
        taskManager.setSmsTaskSettings(smsTaskCreationCheckBox.isChecked(), trustedSmsSenders);
        serena.save();

        setResult(RESULT_OK);
        finish();
    }

    private void onCancelClick()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    private Integer parseMaximumNumberOfTasks()
    {
        try
        {
            int maximumNumberOfTasks = Integer.parseInt(maximumTasksEditText.getText().toString());

            return maximumNumberOfTasks >= 1 ? maximumNumberOfTasks : null;
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private static boolean isValidMaximumNumberOfTasks(EditText editText)
    {
        try
        {
            return Integer.parseInt(editText.getText().toString()) >= 1;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    private void updateMaximumTasksControls()
    {
        boolean isLimited = limitTasksCheckBox.isChecked();

        maximumTasksEditText.setEnabled(isLimited);

        if (isLimited)
        {
            maximumTasksTextView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
        else
        {
            maximumTasksTextView.setTextColor(ContextCompat.getColor(this, R.color.text_disabled));
            maximumTasksEditText.setText("");
        }
    }

    private void showSmsPermissionWarning(int messageResourceId)
    {
        showSmsPermissionWarning(getString(messageResourceId));
    }

    private void showSmsPermissionWarning(String message)
    {
        smsPermissionWarningMessage = message;
        isShowingSmsPermissionWarning = true;
        updateSmsPermissionWarning();
    }

    private void clearSmsPermissionWarning()
    {
        if (smsTaskCreationCheckBox != null && smsTaskCreationCheckBox.isChecked() && !hasSmsPermission())
        {
            showSmsPermissionWarning(R.string.task_sms_permission_unavailable);
            return;
        }

        smsPermissionWarningMessage = null;
        isShowingSmsPermissionWarning = false;
        updateSmsPermissionWarning();
    }

    private void updateSmsPermissionWarning()
    {
        smsPermissionWarningTextView.setText(smsPermissionWarningMessage);
        smsPermissionWarningTextView.setVisibility(isShowingSmsPermissionWarning ? View.VISIBLE : View.GONE);
    }

    private boolean hasSmsPermission()
    {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

}
