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
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

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

    private ArrayAdapter<Task> todaysTasksAdapter;
    private ArrayAdapter<Task> allTasksAdapter;
    private ArrayAdapter<String> menuAdapter;

    private TextView tabTextView;
    private ListView todaysTasksList;
    private ListView allTasksList;
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
            schedule = new Schedule(new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30),
                    new SimpleTaskSelector());
            schedule.addTask(new Task("Write ToDo List", 1, 30,
                    DateTime.now().withTimeAtStartOfDay(), Task.DAILY, 1));
        } else {
            schedule = ScheduleToJsonConverter.convertFromJson(todoJson,
                    new WeeklyEffortTrackerConverter(), new SimpleTaskSelectorConverter());
        }

        todaysTasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        todaysTasksList = (ListView) findViewById(R.id.tasks);
        todaysTasksList.setAdapter(todaysTasksAdapter);
        todaysTasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = todaysTasksAdapter.getItem(position);
                todaysTasksAdapter.remove(task);
                schedule.complete(task);
                allTasksAdapter.clear();
                allTasksAdapter.addAll(schedule.getAllTasks());
            }
        });
        todaysTasksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = schedule.getTasks().get(position);
                schedule.skip(task);
                todaysTasksAdapter.remove(task);
                todaysTasksAdapter.clear();
                todaysTasksAdapter.addAll(schedule.getTasks());
                return true;
            }
        });

        allTasksAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        allTasksList = (ListView) findViewById(R.id.duties);
        allTasksList.setAdapter(allTasksAdapter);
        allTasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Task duty = schedule.getAllTasks().get(position);
                startTaskEditor(duty, ACTIVITY_EDIT_TASK);
            }
        });
        allTasksList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = allTasksAdapter.getItem(position);
                allTasksAdapter.remove(task);
                schedule.removeTask(task);
                todaysTasksAdapter.clear();
                todaysTasksAdapter.addAll(schedule.getTasks());
                return true;
            }
        });

        addButton = (Button) findViewById(R.id.add);

        menuAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                new String[]{"Today's Tasks", "All Tasks", "Effort"});
        menuList = (ListView) findViewById(R.id.menu);
        menuList.setAdapter(menuAdapter);
        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        tabTextView.setText(menuAdapter.getItem(0));
                        todaysTasksList.setVisibility(View.VISIBLE);
                        allTasksList.setVisibility(View.INVISIBLE);
                        addButton.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        tabTextView.setText(menuAdapter.getItem(1));
                        todaysTasksList.setVisibility(View.INVISIBLE);
                        allTasksList.setVisibility(View.VISIBLE);
                        addButton.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        startEffortEditor();
                        break;
                }

                ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
            }
        });

        tabTextView = (TextView) findViewById(R.id.tabTextView);
        tabTextView.setText(menuAdapter.getItem(0));
    }

    @Override
    protected void onStart() {
        super.onStart();

        todaysTasksAdapter.addAll(schedule.getTasks());
        allTasksAdapter.addAll(schedule.getAllTasks());
    }

    @Override
    protected void onStop() {
        super.onStop();

        todaysTasksAdapter.clear();
        allTasksAdapter.clear();

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

    private void handleTaskEditorResult(Intent data, int requestCode) {
        Task duty = taskUnderEdit;

        duty.setNext((DateTime) data.getSerializableExtra("Next"));
        duty.setDescription(data.getStringExtra("Description"));
        duty.setPriority(data.getIntExtra("Priority", 1));
        duty.setEffort(data.getIntExtra("Effort", 1));
        duty.setPeriodicity(data.getIntExtra("Periodicity", 1));
        duty.setFrequency(data.getIntExtra("Frequency", 1));

        if (requestCode == ACTIVITY_ADD_TASK) {
            schedule.addTask(duty);
        }
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
                case ACTIVITY_EDIT_TASK:
                    handleTaskEditorResult(data, requestCode);
                    break;
                case ACTIVITY_EDIT_EFFORT:
                    handleEffortEditorResult(data);
                    break;
            }
        }
    }

    private Schedule createSchedule() {
        Schedule schedule = new Schedule(
                new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30), new SimpleTaskSelector());

        DateTime now = DateTime.now().withTimeAtStartOfDay();

        // Skrivbord
        schedule.addTask(new Task("Skrivbord: Rensa", 7, 2, now, Task.DAILY, 2));
        schedule.addTask(new Task("Skrivbord: Torka", 6, 3, now.plusDays(1), Task.DAILY, 5));
        schedule.addTask(new Task("Skrivbord: Dammsuga", 5, 5, now.plusDays(1), Task.WEEKLY, 1));
        schedule.addTask(new Task("Skrivbord: Moppa", 4, 15, now.withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Soffa och TV
        schedule.addTask(new Task("Soffa och TV: Rensa", 8, 2, now, Task.DAILY, 4));
        schedule.addTask(new Task("Soffa och TV: Torka", 7, 4, now.plusDays(1), Task.DAILY, 6));
        schedule.addTask(new Task("Soffa och TV: Dammsuga", 6, 4, now.plusDays(2), Task.DAILY, 9));
        schedule.addTask(new Task("Soffa och TV: Moppa", 5, 15, now.plusWeeks(1).withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Golv
        schedule.addTask(new Task("Golv: Rensa", 8, 4, now, Task.DAILY, 2));
        schedule.addTask(new Task("Golv: Dammsuga", 6, 10, now.plusDays(2), Task.DAILY, 9));
        schedule.addTask(new Task("Golv: Moppa", 5, 10, now.plusWeeks(2).withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Säng
        schedule.addTask(new Task("Säng: Rensa", 9, 2, now, Task.WEEKLY, 2));
        schedule.addTask(new Task("Säng: Torka", 8, 1, now.plusDays(1), Task.WEEKLY, 1));
        schedule.addTask(new Task("Säng: Dammsuga", 7, 5, now.plusDays(2), Task.WEEKLY, 2));
        schedule.addTask(new Task("Säng: Moppa", 6, 15, now.plusWeeks(3).withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Hall
        schedule.addTask(new Task("Hall: Rensa", 8, 4, now, Task.DAILY, 2));
        schedule.addTask(new Task("Hall: Dammsuga", 6, 5, now.plusDays(2), Task.DAILY, 5));
        schedule.addTask(new Task("Hall: Moppa", 5, 15, now.plusWeeks(4).withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Kök
        schedule.addTask(new Task("Kök: Diska", 6, 10, now, Task.DAILY, 3));
        schedule.addTask(new Task("Kök: Rensa kylskåp", 4, 10, now.plusDays(3), Task.MONTHLY, 1));

        // Badrum
        schedule.addTask(new Task("Badrum: Städa", 6, 10, now.withDayOfWeek(6), Task.WEEKLY, 3));

        // Fönsterkarm
        schedule.addTask(new Task("Fönstarkarm: Rensa", 8, 2, now, Task.WEEKLY, 1));
        schedule.addTask(new Task("Fönstarkarm: Torka", 7, 3, now.plusDays(1), Task.WEEKLY, 1));

        // Lådor
        schedule.addTask(new Task("Lådor: Torka", 7, 5, now, Task.WEEKLY, 2));

        return schedule;
    }

}
