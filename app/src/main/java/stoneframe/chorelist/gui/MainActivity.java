package stoneframe.chorelist.gui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.json.SimpleTaskSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.WeeklyEffortTracker;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int ACTIVITY_EDIT_EFFORT = 0;

    private Schedule schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


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

        GlobalState globalState = (GlobalState)getApplication();
        globalState.setSchedule(schedule);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_effort) {
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

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker) schedule.getEffortTracker();

            effortTracker.setMonday(data.getIntExtra("Monday", 0));
            effortTracker.setTuesday(data.getIntExtra("Tuesday", 0));
            effortTracker.setWednesday(data.getIntExtra("Wednesday", 0));
            effortTracker.setThursday(data.getIntExtra("Thursday", 0));
            effortTracker.setFriday(data.getIntExtra("Friday", 0));
            effortTracker.setSaturday(data.getIntExtra("Saturday", 0));
            effortTracker.setSunday(data.getIntExtra("Sunday", 0));
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment fragment;
        Class fragmentClass;

        switch (item.getItemId()) {
            case R.id.nav_all_tasks:
                fragmentClass = AllTasks.class;
                break;
            case R.id.nav_todays_tasks:
            default:
                fragmentClass = TodaysTasks.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        setTitle(item.getTitle());

        return true;
    }

}
