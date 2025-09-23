package stoneframe.serena.gui.reminders;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.notifications.Notifier;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.reminders.ReminderManager;

public class AllRemindersFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editReminderLauncher;

    private SimpleListAdapter<Reminder> reminderListAdapter;

    private GlobalState globalState;
    private Serena serena;
    private ReminderManager reminderManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();
        reminderManager = serena.getReminderManager();

        View rootView = inflater.inflate(R.layout.fragment_all_reminders, container, false);

        reminderListAdapter = new SimpleListAdapterBuilder<>(
            requireContext(),
            reminderManager::getAllReminders,
            Reminder::getText)
            .withSecondaryTextFunction(r -> r.getDateTime().toString("yyyy-MM-dd HH:mm"))
            .create();

        ListView reminderListView = rootView.findViewById(R.id.all_reminders);
        reminderListView.setAdapter(reminderListAdapter);
        reminderListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Reminder reminder = (Reminder)reminderListAdapter.getItem(position);
            assert reminder != null;
            startReminderEditor(reminder, EditReminderActivity.ACTION_EDIT);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Reminder reminder = reminderManager.createReminder();

            startReminderEditor(reminder, EditReminderActivity.ACTION_ADD);
        });

        editReminderLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editReminderCallback);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        reminderListAdapter.notifyDataSetChanged();
    }

    private void startReminderEditor(Reminder reminder, int action)
    {
        globalState.setActiveReminder(reminder);

        Intent intent = new Intent(getActivity(), EditReminderActivity.class)
            .putExtra("ACTION", action);

        editReminderLauncher.launch(intent);
    }

    private void editReminderCallback(ActivityResult activityResult)
    {
        reminderListAdapter.notifyDataSetChanged();

        Notifier.scheduleAlarm(requireContext(), serena);
    }
}