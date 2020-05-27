package stoneframe.chorelist.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import stoneframe.chorelist.R;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.json.SimpleTaskSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.WeeklyEffortTracker;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener
{

    private static final String SCHEDULE_SAVE_NAME = Schedule.class.getName();
    private static final int ACTIVITY_EDIT_EFFORT = 0;
    private static final int ACTIVITY_EDIT_SCHEDULE = 1;

    private Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences settings = getPreferences(0);

        String json = settings.getString(SCHEDULE_SAVE_NAME, null);

        if (json == null)
        {
            json = settings.getString("Schedule2", null);
        }

        if (json == null)
        {
            schedule = new Schedule(
                new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30),
                new SimpleTaskSelector());
            schedule.addTask(new Task(
                "Write ToDo List",
                1,
                30,
                DateTime.now().withTimeAtStartOfDay(),
                Task.DAILY,
                1));
        }
        else
        {
            schedule = ScheduleToJsonConverter.convertFromJson(
                json,
                new WeeklyEffortTrackerConverter(),
                new SimpleTaskSelectorConverter());
        }

        GlobalState globalState = (GlobalState)getApplication();
        globalState.setSchedule(schedule);

        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        SharedPreferences settings = getPreferences(0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(SCHEDULE_SAVE_NAME, ScheduleToJsonConverter.convertToJson(schedule));

        editor.commit();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.action_effort)
        {
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)schedule.getEffortTracker();

            Intent intent = new Intent(this, EffortActivity.class);

            intent.putExtra("Monday", effortTracker.getMonday());
            intent.putExtra("Tuesday", effortTracker.getTuesday());
            intent.putExtra("Wednesday", effortTracker.getWednesday());
            intent.putExtra("Thursday", effortTracker.getThursday());
            intent.putExtra("Friday", effortTracker.getFriday());
            intent.putExtra("Saturday", effortTracker.getSaturday());
            intent.putExtra("Sunday", effortTracker.getSunday());

            startActivityForResult(intent, ACTIVITY_EDIT_EFFORT);

            return true;
        }

        if (id == R.id.activity_schedule)
        {
            String json = ScheduleToJsonConverter.convertToJson(schedule);

            Intent intent = new Intent(this, ScheduleActivity.class);

            intent.putExtra("Schedule", json);

            startActivityForResult(intent, ACTIVITY_EDIT_SCHEDULE);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == ACTIVITY_EDIT_EFFORT)
        {
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)schedule.getEffortTracker();

            effortTracker.setMonday(data.getIntExtra("Monday", 0));
            effortTracker.setTuesday(data.getIntExtra("Tuesday", 0));
            effortTracker.setWednesday(data.getIntExtra("Wednesday", 0));
            effortTracker.setThursday(data.getIntExtra("Thursday", 0));
            effortTracker.setFriday(data.getIntExtra("Friday", 0));
            effortTracker.setSaturday(data.getIntExtra("Saturday", 0));
            effortTracker.setSunday(data.getIntExtra("Sunday", 0));
        }

        if (resultCode == RESULT_OK && requestCode == ACTIVITY_EDIT_SCHEDULE)
        {
            String json = data.getStringExtra("Schedule");

            schedule = ScheduleToJsonConverter.convertFromJson(
                json,
                new WeeklyEffortTrackerConverter(),
                new SimpleTaskSelectorConverter());

            GlobalState globalState = (GlobalState)getApplication();
            globalState.setSchedule(schedule);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Fragment fragment;
        Class fragmentClass;

        switch (item.getItemId())
        {
            case R.id.nav_all_tasks:
                fragmentClass = AllTasks.class;
                break;
            case R.id.nav_todays_tasks:
            default:
                fragmentClass = TodaysTasks.class;
        }

        try
        {
            fragment = (Fragment)fragmentClass.newInstance();
        } catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();


        DrawerLayout drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        setTitle(item.getTitle());

        return true;
    }

    private Schedule createSchedule()
    {
        Schedule schedule = new Schedule(
            new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30), new SimpleTaskSelector());

        DateTime now = DateTime.now().withTimeAtStartOfDay();

        // Badrum
        schedule.addTask(new Task("Badrum: Städa", 6, 10, now.withDayOfWeek(6), Task.WEEKLY, 3));

        // Boka
        schedule.addTask(new Task("Boka: Klipptid", 3, 1, now.withDayOfMonth(1), Task.MONTHLY, 2));
        schedule.addTask(new Task("Boka: Synundersökning", 3, 1, now.withMonthOfYear(3)
            .withDayOfMonth(1), Task.YEARLY, 1));

        // Födelsedagar
        schedule.addTask(new Task("Födelsedag: Jonathan Karlsson", 1, 0, now.withMonthOfYear(9)
            .withDayOfMonth(2), Task.YEARLY, 1));
        schedule.addTask(new Task("Födelsedag: Jonathan Lundholm", 1, 0, now.withMonthOfYear(11)
            .withDayOfMonth(3), Task.YEARLY, 1));
        schedule.addTask(new Task("Födelsedag: Kajsa Binder", 1, 0, now.withMonthOfYear(12)
            .withDayOfMonth(23), Task.YEARLY, 1));

        // Fönsterkarm
        schedule.addTask(new Task("Fönsterkarm: Rensa", 8, 2, now, Task.WEEKLY, 1));
        schedule.addTask(new Task("Fönsterkarm: Torka", 7, 3, now.plusDays(1), Task.WEEKLY, 1));

        // Golv
        schedule.addTask(new Task("Golv: Rensa", 8, 4, now, Task.DAILY, 2));
        schedule.addTask(new Task("Golv: Dammsuga", 6, 10, now.plusDays(2), Task.DAILY, 9));
        schedule.addTask(new Task("Golv: Moppa", 5, 10, now.plusWeeks(2)
            .withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Hall
        schedule.addTask(new Task("Hall: Rensa", 8, 4, now, Task.DAILY, 2));
        schedule.addTask(new Task("Hall: Dammsuga", 6, 5, now.plusDays(2), Task.DAILY, 5));
        schedule.addTask(new Task("Hall: Moppa", 5, 15, now.plusWeeks(4)
            .withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Kök
        schedule.addTask(new Task("Kök: Diska", 6, 10, now, Task.DAILY, 3));
        schedule.addTask(new Task("Kök: Rensa kylskåp", 4, 10, now.plusDays(3), Task.MONTHLY, 1));

        // Lådor
        schedule.addTask(new Task("Lådor: Torka", 7, 5, now, Task.WEEKLY, 2));

        // Skrivbord
        schedule.addTask(new Task("Skrivbord: Rensa", 7, 2, now, Task.DAILY, 1));
        schedule.addTask(new Task("Skrivbord: Torka", 6, 3, now.plusDays(1), Task.DAILY, 5));
        schedule.addTask(new Task("Skrivbord: Dammsuga", 5, 5, now.plusDays(1), Task.WEEKLY, 1));
        schedule.addTask(new Task(
            "Skrivbord: Moppa",
            4,
            15,
            now.withDayOfWeek(DateTimeConstants.SATURDAY),
            Task.WEEKLY,
            6));

        // Soffa och TV
        schedule.addTask(new Task("Soffa och TV: Rensa", 8, 2, now, Task.DAILY, 4));
        schedule.addTask(new Task("Soffa och TV: Torka", 7, 4, now.plusDays(1), Task.DAILY, 6));
        schedule.addTask(new Task("Soffa och TV: Dammsuga", 6, 4, now.plusDays(2), Task.DAILY, 9));
        schedule.addTask(new Task("Soffa och TV: Moppa", 5, 15, now.plusWeeks(1)
            .withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        // Säng
        schedule.addTask(new Task("Säng: Rensa", 9, 2, now, Task.WEEKLY, 2));
        schedule.addTask(new Task("Säng: Torka", 8, 1, now.plusDays(1), Task.WEEKLY, 1));
        schedule.addTask(new Task("Säng: Dammsuga", 7, 5, now.plusDays(2), Task.WEEKLY, 2));
        schedule.addTask(new Task("Säng: Moppa", 6, 15, now.plusWeeks(3)
            .withDayOfWeek(DateTimeConstants.SATURDAY), Task.WEEKLY, 6));

        return schedule;
    }

}
