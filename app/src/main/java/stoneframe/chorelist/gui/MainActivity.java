package stoneframe.chorelist.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.json.SimpleTaskSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.model.WeeklyEffortTracker;

public class MainActivity extends AppCompatActivity {

    private static final int ACTIVITY_ADD_TASK = 0;
    private static final int ACTIVITY_EDIT_TASK = 1;
    private static final int ACTIVITY_EDIT_EFFORT = 2;

    private ArrayAdapter<Task> taskAdapter;
    private ArrayAdapter<Task> dutyAdapter;
    private ArrayAdapter<String> menuAdapter;

    private ListView taskList;
    private ListView dutyList;
    private ListView menuList;
    private Button addButton;

    private Schedule schedule;

    private Task taskUnderEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getPreferences(0);

        String todoJson = settings.getString("Schedule", null);
        if (todoJson == null) {
            schedule = createToDoList();
        } else {
            schedule = ScheduleToJsonConverter.convertFromJson(todoJson,
                    new SimpleTaskSelectorConverter(), new WeeklyEffortTrackerConverter());
        }

        taskAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        taskList = (ListView) findViewById(R.id.tasks);
        taskList.setAdapter(taskAdapter);
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = taskAdapter.getItem(position);
                taskAdapter.remove(task);
                schedule.complete(task);
                dutyAdapter.clear();
                dutyAdapter.addAll(schedule.getAllTasks());
            }
        });
        taskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = schedule.getTasks().get(position);
                schedule.skip(task);
                taskAdapter.remove(task);
                taskAdapter.clear();
                taskAdapter.addAll(schedule.getTasks());
                return true;
            }
        });

        dutyAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        dutyList = (ListView) findViewById(R.id.duties);
        dutyList.setAdapter(dutyAdapter);
        dutyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task duty = schedule.getAllTasks().get(position);
                startTaskEditor(duty, ACTIVITY_EDIT_TASK);
            }
        });
        dutyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = dutyAdapter.getItem(position);
                dutyAdapter.remove(task);
                schedule.removeTask(task);
                taskAdapter.clear();
                taskAdapter.addAll(schedule.getTasks());
                return true;
            }
        });

        addButton = (Button) findViewById(R.id.add);

        menuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Today's Tasks", "Schedule", "Effort"});
        menuList = (ListView) findViewById(R.id.menu);
        menuList.setAdapter(menuAdapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        taskList.setVisibility(View.VISIBLE);
                        dutyList.setVisibility(View.INVISIBLE);
                        addButton.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        taskList.setVisibility(View.INVISIBLE);
                        dutyList.setVisibility(View.VISIBLE);
                        addButton.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        startEffortEditor();
                        break;
                }

                ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        taskAdapter.addAll(schedule.getTasks());
        dutyAdapter.addAll(schedule.getAllTasks());
    }

    @Override
    protected void onStop() {
        super.onStop();

        taskAdapter.clear();
        dutyAdapter.clear();

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("Schedule", ScheduleToJsonConverter.convertToJson(schedule));

        editor.commit();
    }

    public void addTaskClick(View view) {
        Task duty = new Task("", 1, 1, DateTime.now().withTimeAtStartOfDay(), Task.DAILY, 1);
        startTaskEditor(duty, ACTIVITY_ADD_TASK);
    }

    private void startTaskEditor(Task task, int mode) {
        taskUnderEdit = task;

        Intent intent = new Intent(this, TaskActivity.class);
        intent.putExtra("Next", task.getNext());
        intent.putExtra("Description", task.getDescription());
        intent.putExtra("Priority", task.getPriority());
        intent.putExtra("Effort", task.getEffort());
        intent.putExtra("Periodicity", task.getPeriodicity());
        intent.putExtra("Frequency", task.getFrequency());

        startActivityForResult(intent, mode);
    }

    private void handleTaskEditorResult(Intent data) {
        Task duty = taskUnderEdit;

        duty.setNext((DateTime) data.getSerializableExtra("Next"));
        duty.setDescription(data.getStringExtra("Description"));
        duty.setPriority(data.getIntExtra("Priority", 1));
        duty.setEffort(data.getIntExtra("Effort", 1));
        duty.setPeriodicity(data.getIntExtra("Periodicity", 1));
        duty.setFrequency(data.getIntExtra("Frequency", 1));

        schedule.addTask(duty);
    }

    private void startEffortEditor() {
        WeeklyEffortTracker effortTracker = (WeeklyEffortTracker) schedule.getEffortTracker();

        Intent intent = new Intent(this, EffortActivity.class);

        intent.putExtra("Monday", effortTracker.getMonday());
        intent.putExtra("Tuesday", effortTracker.getTuesday());
        intent.putExtra("Wednesday", effortTracker.getWednesday());
        intent.putExtra("Thursday", effortTracker.getThursday());
        intent.putExtra("Friday", effortTracker.getFriday());
        intent.putExtra("Saturday", effortTracker.getSaturday());
        intent.putExtra("Sunday", effortTracker.getSunday());

        startActivityForResult(intent, ACTIVITY_EDIT_EFFORT);
    }

    private void handleEffortEditorResult(Intent data) {
        WeeklyEffortTracker effortTracker = (WeeklyEffortTracker) schedule.getEffortTracker();

        effortTracker.setMonday(data.getIntExtra("Monday", 0));
        effortTracker.setTuesday(data.getIntExtra("Tuesday", 0));
        effortTracker.setWednesday(data.getIntExtra("Wednesday", 0));
        effortTracker.setThursday(data.getIntExtra("Thursday", 0));
        effortTracker.setFriday(data.getIntExtra("Friday", 0));
        effortTracker.setSaturday(data.getIntExtra("Saturday", 0));
        effortTracker.setSunday(data.getIntExtra("Sunday", 0));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ACTIVITY_ADD_TASK:
                    handleTaskEditorResult(data);
                    break;
                case ACTIVITY_EDIT_EFFORT:
                    handleEffortEditorResult(data);
                    break;
            }
        }
    }

    private Schedule createToDoList() {
        Schedule schedule = new Schedule(
                new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30), new SimpleTaskSelector());

        DateTime now = DateTime.now().withTimeAtStartOfDay();

        // Skrivbord
        schedule.addTask(new Task("Skrivbord: Rensa", 5, 2, now, Task.DAILY, 2));
        schedule.addTask(new Task("Skrivbord: Torka", 4, 3, now.plusDays(1), Task.DAILY, 5));
        schedule.addTask(new Task("Skrivbord: Dammsuga", 3, 5, now.plusDays(1), Task.WEEKLY, 1));

        // Soffa och TV
        schedule.addTask(new Task("Soffa och TV: Rensa", 6, 2, now, Task.DAILY, 4));
        schedule.addTask(new Task("Soffa och TV: Torka", 5, 4, now.plusDays(1), Task.DAILY, 6));
        schedule.addTask(new Task("Soffa och TV: Dammsuga", 4, 4, now.plusDays(2), Task.DAILY, 9));

        // Golv
        schedule.addTask(new Task("Golv: Rensa", 6, 4, now, Task.DAILY, 2));
        schedule.addTask(new Task("Golv: Dammsuga", 4, 10, now.plusDays(2), Task.DAILY, 9));

        // Säng
        schedule.addTask(new Task("Säng: Rensa", 7, 2, now, Task.WEEKLY, 2));
        schedule.addTask(new Task("Säng: Torka", 6, 1, now.plusDays(1), Task.WEEKLY, 1));
        schedule.addTask(new Task("Säng: Dammsuga", 6, 5, now.plusDays(2), Task.WEEKLY, 2));

        // Hall
        schedule.addTask(new Task("Hall: Rensa", 6, 4, now, Task.DAILY, 2));
        schedule.addTask(new Task("Hall: Dammsuga", 4, 5, now.plusDays(2), Task.DAILY, 5));

        // Kök
        schedule.addTask(new Task("Kök: Diska", 4, 10, now, Task.DAILY, 3));
        schedule.addTask(new Task("Kök: Rensa kylskåp", 2, 10, now.plusDays(3), Task.MONTHLY, 1));

        // Badrum
        schedule.addTask(new Task("Badrum: Städa", 4, 10, now.withDayOfWeek(6), Task.WEEKLY, 3));

        return schedule;
    }

}
