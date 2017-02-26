package stoneframe.chorelist.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.json.SimpleEffortTrackerConverter;
import stoneframe.chorelist.json.SimpleTaskSelectorConverter;
import stoneframe.chorelist.model.Duty;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.ToDoList;
import stoneframe.chorelist.json.TodoListToJsonConverter;

public class MainActivity extends AppCompatActivity {

    private static final int ADD_DUTY = 0;
    private static final int EDIT_DUTY = 1;

    private TaskHandler taskHandler;
    private DutyHandler dutyHandler;

    private ListView taskList;
    private ListView dutyList;
    private ListView menuList;

    private ToDoList todoList;

    private Duty dutyUnderEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences settings = getPreferences(0);

        final String todoJson = settings.getString("ToDoList", null);
        if (todoJson == null) {
            todoList = createToDoList();
        } else {
            todoList = TodoListToJsonConverter.convertFromJson(todoJson,
                    new SimpleTaskSelectorConverter(), new SimpleEffortTrackerConverter());
        }

        taskHandler = new TaskHandler(this, android.R.layout.simple_list_item_1, todoList);
        taskList = (ListView) findViewById(R.id.tasks);
        taskList.setAdapter(taskHandler);
        taskList.setOnItemClickListener(taskHandler);
        taskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Task task = todoList.getTasks(DateTime.now()).get(position);
                todoList.skip(task);
                taskHandler.update();
                return true;
            }
        });

        dutyHandler = new DutyHandler(this, android.R.layout.simple_list_item_1, todoList.getSchedule());
        dutyList = (ListView) findViewById(R.id.duties);
        dutyList.setAdapter(dutyHandler);
        dutyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Duty duty = todoList.getSchedule().getDuties().get(position);
                startDutyEditor(duty, EDIT_DUTY);
            }
        });
        dutyList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Duty duty = todoList.getSchedule().getDuties().get(position);
                todoList.getSchedule().removeDuty(duty);
                dutyHandler.update();
                taskHandler.update();
                return true;
            }
        });

        Button addButton = (Button) findViewById(R.id.add);

        MenuHandler menuHandler = new MenuHandler(this, android.R.layout.simple_list_item_1);
        menuHandler.addView(0, taskList);
        menuHandler.addView(1, dutyList);
        menuHandler.addView(1, addButton);
        menuList = (ListView) findViewById(R.id.menu);
        menuList.setAdapter(menuHandler);
        menuList.setOnItemClickListener(menuHandler);
    }

    @Override
    protected void onStart() {
        super.onStart();

        taskHandler.update();
        dutyHandler.update();
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString("ToDoList", TodoListToJsonConverter.convertToJson(todoList));

        editor.commit();
    }

    private static void writeToIntent(Duty duty, Intent intent) {
        intent.putExtra("Next", duty.getNext());
        intent.putExtra("Description", duty.getDescription());
        intent.putExtra("Priority", duty.getPriority());
        intent.putExtra("Effort", duty.getEffort());
        intent.putExtra("Periodicity", duty.getPeriodicity());
        intent.putExtra("Frequency", duty.getFrequency());
        intent.putExtra("Day", duty.getDay());
    }

    private static void readFromIntent(Duty duty, Intent intent) {
        DateTime next = (DateTime) intent.getSerializableExtra("Next");
        String description = intent.getStringExtra("Description");
        int priority = intent.getIntExtra("Priority", 1);
        int effort = intent.getIntExtra("Effort", 1);
        int periodicity = intent.getIntExtra("Periodicity", 1);
        int frequency = intent.getIntExtra("Frequency", 1);
        int day = intent.getIntExtra("Day", 1);

        duty.setNext(next);
        duty.setDescription(description);
        duty.setPriority(priority);
        duty.setEffort(effort);
        duty.setPeriodicity(periodicity);
        duty.setFrequency(frequency);
        duty.setDay(day);
    }

    public void addDutyClick(View view) {
        Duty duty = new Duty("", 1, 1, DateTime.now().withTimeAtStartOfDay(), Duty.DAILY, 1);
        startDutyEditor(duty, ADD_DUTY);
    }

    private void startDutyEditor(Duty duty, int mode) {
        dutyUnderEdit = duty;
        Intent intent = new Intent(this, EditDutyActivity.class);
        writeToIntent(duty, intent);
        startActivityForResult(intent, mode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Duty duty = dutyUnderEdit;
            readFromIntent(duty, data);

            if (requestCode == ADD_DUTY) {
                todoList.getSchedule().addDuty(duty);
            }
        }
    }

    private ToDoList createToDoList() {
        Schedule schedule = new Schedule();

        DateTime now = DateTime.now().withTimeAtStartOfDay();

        // Skrivbord
        schedule.addDuty(new Duty("Skrivbord: Rensa", 5, 2, now, Duty.DAILY, 2));
        schedule.addDuty(new Duty("Skrivbord: Torka", 4, 3, now.plusDays(1), Duty.DAILY, 5));
        schedule.addDuty(new Duty("Skrivbord: Dammsuga", 3, 5, now.plusDays(1), Duty.WEEKLY, 1));

        // Soffa och TV
        schedule.addDuty(new Duty("Soffa och TV: Rensa", 6, 2, now, Duty.DAILY, 4));
        schedule.addDuty(new Duty("Soffa och TV: Torka", 5, 4, now.plusDays(1), Duty.DAILY, 6));
        schedule.addDuty(new Duty("Soffa och TV: Dammsuga", 4, 4, now.plusDays(2), Duty.DAILY, 9));

        // Golv
        schedule.addDuty(new Duty("Golv: Rensa", 6, 4, now, Duty.DAILY, 2));
        schedule.addDuty(new Duty("Golv: Dammsuga", 4, 10, now.plusDays(2), Duty.DAILY, 9));

        // Säng
        schedule.addDuty(new Duty("Säng: Rensa", 7, 2, now, Duty.WEEKLY, 2));
        schedule.addDuty(new Duty("Säng: Torka", 6, 1, now.plusDays(1), Duty.WEEKLY, 1));
        schedule.addDuty(new Duty("Säng: Dammsuga", 6, 5, now.plusDays(2), Duty.WEEKLY, 2));

        // Hall
        schedule.addDuty(new Duty("Hall: Rensa", 6, 4, now, Duty.DAILY, 2));
        schedule.addDuty(new Duty("Hall: Dammsuga", 4, 5, now.plusDays(2), Duty.DAILY, 5));

        // Kök
        schedule.addDuty(new Duty("Kök: Diska", 4, 10, now, Duty.DAILY, 3));
        schedule.addDuty(new Duty("Kök: Rensa kylskåp", 2, 10, now.plusDays(3), Duty.MONTLY, 1));

        // Badrum
        schedule.addDuty(new Duty("Badrum: Städa", 4, 10, now.withDayOfWeek(6), Duty.WEEKLY, 3));

        ToDoList todoList = new ToDoList(schedule,
                new SimpleTaskSelector(), new SimpleEffortTracker(15));

        return todoList;
    }

}
