package stoneframe.chorelist.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import stoneframe.chorelist.R;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.WeeklyEffortTracker;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String SCHEDULE_SAVE_NAME = ChoreManager.class.getName();
    private static final int ACTIVITY_EDIT_EFFORT = 0;
    private static final int ACTIVITY_EDIT_SCHEDULE = 1;

    private ChoreManager choreManager;

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
            choreManager = new ChoreManager(
                new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30),
                new SimpleChoreSelector());
            choreManager.addChore(new Chore(
                "Write ToDo List",
                1,
                30,
                DateTime.now().withTimeAtStartOfDay(),
                1, Chore.DAYS
            ));
        }
        else
        {
            choreManager = ScheduleToJsonConverter.convertFromJson(
                json,
                new WeeklyEffortTrackerConverter(),
                new SimpleChoreSelectorConverter());
        }

        GlobalState globalState = (GlobalState)getApplication();
        globalState.setSchedule(choreManager);

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

        editor.putString(SCHEDULE_SAVE_NAME, ScheduleToJsonConverter.convertToJson(choreManager));

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
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)choreManager.getEffortTracker();

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
            String json = ScheduleToJsonConverter.convertToJson(choreManager);

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
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)choreManager.getEffortTracker();

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

            choreManager = ScheduleToJsonConverter.convertFromJson(
                json,
                new WeeklyEffortTrackerConverter(),
                new SimpleChoreSelectorConverter());

            GlobalState globalState = (GlobalState)getApplication();
            globalState.setSchedule(choreManager);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Fragment fragment;
        Class fragmentClass;

        switch (item.getItemId())
        {
            case R.id.nav_all_chores:
                fragmentClass = AllChores.class;
                break;
            case R.id.nav_todays_chores:
            default:
                fragmentClass = TodaysChores.class;
        }

        try
        {
            fragment = (Fragment)fragmentClass.newInstance();
        }
        catch (Exception e)
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

    private ChoreManager createSchedule()
    {
        ChoreManager choreManager = new ChoreManager(
            new WeeklyEffortTracker(15, 15, 15, 15, 15, 30, 30), new SimpleChoreSelector());

        DateTime now = DateTime.now().withTimeAtStartOfDay();

        // Badrum
        choreManager.addChore(new Chore("Badrum: Städa", 6, 10, now.withDayOfWeek(6), 3, Chore.WEEKS));

        // Boka
        choreManager.addChore(new Chore("Boka: Klipptid", 3, 1, now.withDayOfMonth(1), 2, Chore.MONTHS));
        choreManager.addChore(new Chore("Boka: Synundersökning", 3, 1, now.withMonthOfYear(3)
            .withDayOfMonth(1), 1, Chore.YEARS));

        // Födelsedagar
        choreManager.addChore(new Chore("Födelsedag: Jonathan Karlsson", 1, 0, now.withMonthOfYear(9)
            .withDayOfMonth(2), 1, Chore.YEARS));
        choreManager.addChore(new Chore("Födelsedag: Jonathan Lundholm", 1, 0, now.withMonthOfYear(11)
            .withDayOfMonth(3), 1, Chore.YEARS));
        choreManager.addChore(new Chore("Födelsedag: Kajsa Binder", 1, 0, now.withMonthOfYear(12)
            .withDayOfMonth(23), 1, Chore.YEARS));

        // Fönsterkarm
        choreManager.addChore(new Chore("Fönsterkarm: Rensa", 8, 2, now, 1, Chore.WEEKS));
        choreManager.addChore(new Chore("Fönsterkarm: Torka", 7, 3, now.plusDays(1), 1, Chore.WEEKS));

        // Golv
        choreManager.addChore(new Chore("Golv: Rensa", 8, 4, now, 2, Chore.DAYS));
        choreManager.addChore(new Chore("Golv: Dammsuga", 6, 10, now.plusDays(2), 9, Chore.DAYS));
        choreManager.addChore(new Chore("Golv: Moppa", 5, 10, now.plusWeeks(2)
            .withDayOfWeek(DateTimeConstants.SATURDAY), 6, Chore.WEEKS));

        // Hall
        choreManager.addChore(new Chore("Hall: Rensa", 8, 4, now, 2, Chore.DAYS));
        choreManager.addChore(new Chore("Hall: Dammsuga", 6, 5, now.plusDays(2), 5, Chore.DAYS));
        choreManager.addChore(new Chore("Hall: Moppa", 5, 15, now.plusWeeks(4)
            .withDayOfWeek(DateTimeConstants.SATURDAY), 6, Chore.WEEKS));

        // Kök
        choreManager.addChore(new Chore("Kök: Diska", 6, 10, now, 3, Chore.DAYS));
        choreManager.addChore(new Chore("Kök: Rensa kylskåp", 4, 10, now.plusDays(3), 1, Chore.MONTHS));

        // Lådor
        choreManager.addChore(new Chore("Lådor: Torka", 7, 5, now, 2, Chore.WEEKS));

        // Skrivbord
        choreManager.addChore(new Chore("Skrivbord: Rensa", 7, 2, now, 1, Chore.DAYS));
        choreManager.addChore(new Chore("Skrivbord: Torka", 6, 3, now.plusDays(1), 5, Chore.DAYS));
        choreManager.addChore(new Chore("Skrivbord: Dammsuga", 5, 5, now.plusDays(1), 1, Chore.WEEKS));
        choreManager.addChore(new Chore(
            "Skrivbord: Moppa",
            4,
            15,
            now.withDayOfWeek(DateTimeConstants.SATURDAY),
            6, Chore.WEEKS
        ));

        // Soffa och TV
        choreManager.addChore(new Chore("Soffa och TV: Rensa", 8, 2, now, 4, Chore.DAYS));
        choreManager.addChore(new Chore("Soffa och TV: Torka", 7, 4, now.plusDays(1), 6, Chore.DAYS));
        choreManager.addChore(new Chore("Soffa och TV: Dammsuga", 6, 4, now.plusDays(2), 9, Chore.DAYS));
        choreManager.addChore(new Chore("Soffa och TV: Moppa", 5, 15, now.plusWeeks(1)
            .withDayOfWeek(DateTimeConstants.SATURDAY), 6, Chore.WEEKS));

        // Säng
        choreManager.addChore(new Chore("Säng: Rensa", 9, 2, now, 2, Chore.WEEKS));
        choreManager.addChore(new Chore("Säng: Torka", 8, 1, now.plusDays(1), 1, Chore.WEEKS));
        choreManager.addChore(new Chore("Säng: Dammsuga", 7, 5, now.plusDays(2), 2, Chore.WEEKS));
        choreManager.addChore(new Chore("Säng: Moppa", 6, 15, now.plusWeeks(3)
            .withDayOfWeek(DateTimeConstants.SATURDAY), 6, Chore.WEEKS));

        return choreManager;
    }
}
