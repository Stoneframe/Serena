package stoneframe.chorelist.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import org.joda.time.LocalDateTime;

import java.util.Objects;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.checklists.AllChecklistsFragment;
import stoneframe.chorelist.gui.chores.AllChoresFragment;
import stoneframe.chorelist.gui.limiters.AllLimitersFragment;
import stoneframe.chorelist.gui.routines.AllRoutinesFragment;
import stoneframe.chorelist.gui.routines.RoutineNotifier;
import stoneframe.chorelist.gui.tasks.AllTasksFragment;
import stoneframe.chorelist.gui.today.TodayFragment;
import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.storages.JsonConverter;
import stoneframe.chorelist.model.storages.SharedPreferencesStorage;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActivityResultLauncher<Intent> editStorageLauncher;

    private Class<?> fragmentClass;

    private ChoreList choreList;
    private Storage storage;

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

        if (id == R.id.activity_storage)
        {
            choreList.save();

            String json = ContainerJsonConverter.toJson(storage.load());

            Intent intent = new Intent(this, StorageActivity.class);

            intent.putExtra("Storage", json);

            editStorageLauncher.launch(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_all_chores:
                return goToFragment(AllChoresFragment.class);
            case R.id.nav_all_tasks:
                return goToFragment(AllTasksFragment.class);
            case R.id.nav_all_routines:
                return goToFragment(AllRoutinesFragment.class);
            case R.id.nav_all_checklists:
                return goToFragment(AllChecklistsFragment.class);
            case R.id.nav_calories:
                return goToFragment(AllLimitersFragment.class);
            case R.id.nav_todays:
            default:
                return goToFragment(TodayFragment.class);
        }
    }

    private boolean goToFragment(Class<?> fragmentClass)
    {
        this.fragmentClass = fragmentClass;

        try
        {
            Fragment fragment = (Fragment)this.fragmentClass.newInstance();

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

            setTitle(getFragmentTitle(fragmentClass));
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
    }

    private String getFragmentTitle(Class<?> fragmentClass)
    {
        if (fragmentClass == TodayFragment.class) return "Today";
        if (fragmentClass == AllRoutinesFragment.class) return "Routines";
        if (fragmentClass == AllChoresFragment.class) return "Chores";
        if (fragmentClass == AllTasksFragment.class) return "Tasks";
        if (fragmentClass == AllChecklistsFragment.class) return "Checklists";
        if (fragmentClass == AllLimitersFragment.class) return "Limiters";

        return "Chore List";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Thread.setDefaultUncaughtExceptionHandler(new MyExceptionHandler(this));

        GlobalState globalState = (GlobalState)getApplication();

        choreList = globalState.getChoreList();

        if (storage == null)
        {
            storage = new SharedPreferencesStorage(
                this,
                new JsonConverter(
                    new SimpleChoreSelectorConverter(),
                    new WeeklyEffortTrackerConverter()));
        }

        if (choreList == null)
        {
            choreList = new ChoreList(
                storage,
                new RealTimeService(),
                new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
                new SimpleChoreSelector());

            choreList.load();

            globalState.setChoreList(choreList);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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

        RoutineNotifier.setupNotificationChannel(this);

        LocalDateTime nextRoutineProcedureTime = choreList.getNextRoutineProcedureTime();

        if (nextRoutineProcedureTime != null)
        {
            RoutineNotifier.scheduleRoutineAlarm(this, nextRoutineProcedureTime);
        }

        editStorageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editStorageCallback);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed()
            {
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                if (drawer.isDrawerOpen(GravityCompat.START))
                {
                    drawer.closeDrawer(GravityCompat.START);
                }
                else if (fragmentClass != TodayFragment.class)
                {
                    goToFragment(TodayFragment.class);

                    NavigationView navigationView = findViewById(R.id.nav_view);

                    navigationView.setCheckedItem(R.id.nav_todays);
                }
                else
                {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        choreList.save();
    }

    private void editStorageCallback(ActivityResult activityResult)
    {
        if (activityResult.getResultCode() != RESULT_OK)
        {
            return;
        }

        Intent data = Objects.requireNonNull(activityResult.getData());

        String json = data.getStringExtra("Storage");

        SharedPreferences sharedPreferences = getSharedPreferences("DATA", 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("DATA", json);

        editor.apply();

        choreList.load();
    }
}
