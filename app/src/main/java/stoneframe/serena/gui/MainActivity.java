package stoneframe.serena.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

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

import stoneframe.serena.R;
import stoneframe.serena.balancers.BalancerManager;
import stoneframe.serena.gui.balancers.AllBalancersFragment;
import stoneframe.serena.gui.checklists.AllChecklistsFragment;
import stoneframe.serena.gui.chores.AllChoresFragment;
import stoneframe.serena.gui.notes.AllNotesFragment;
import stoneframe.serena.gui.routines.AllRoutinesFragment;
import stoneframe.serena.gui.routines.RoutineNotifier;
import stoneframe.serena.gui.sleep.SleepFragment;
import stoneframe.serena.gui.tasks.AllTasksFragment;
import stoneframe.serena.gui.today.TodayFragment;
import stoneframe.serena.sleep.SleepManager;
import stoneframe.serena.storages.json.ContainerJsonConverter;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.storages.JsonConverter;
import stoneframe.serena.storages.SharedPreferencesStorage;
import stoneframe.serena.timeservices.RealTimeService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActivityResultLauncher<Intent> editStorageLauncher;

    private int selectedFragment;

    private Serena serena;
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
            serena.save();

            String json = ContainerJsonConverter.toJson(storage.load());

            Intent intent = new Intent(this, StorageActivity.class);

            intent.putExtra("Storage", json);

            editStorageLauncher.launch(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int itemId = item.getItemId();

        return goToFragment(itemId);
    }

    @SuppressLint("NonConstantResourceId")
    private boolean goToFragment(int fragmentId)
    {
        selectedFragment = fragmentId;

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(fragmentId);

        switch (fragmentId)
        {
            case R.id.nav_all_chores:
                return goToFragment(AllChoresFragment.class);
            case R.id.nav_all_tasks:
                return goToFragment(AllTasksFragment.class);
            case R.id.nav_all_routines:
                return goToFragment(AllRoutinesFragment.class);
            case R.id.nav_all_checklists:
                return goToFragment(AllChecklistsFragment.class);
            case R.id.nav_balancers:
                return goToFragment(AllBalancersFragment.class);
            case R.id.nav_sleep:
                return goToFragment(SleepFragment.class);
            case R.id.nav_notes:
                return goToFragment(AllNotesFragment.class);
            case R.id.nav_todays:
            default:
                return goToFragment(TodayFragment.class);
        }
    }

    private boolean goToFragment(Class<?> fragmentClass)
    {
        try
        {
            Fragment fragment = (Fragment)fragmentClass.newInstance();

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
        if (fragmentClass == SleepFragment.class) return "Sleep";
        if (fragmentClass == AllBalancersFragment.class) return "Balancers";

        return "Serena";
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

        serena = globalState.getSerena();

        if (storage == null)
        {
            storage = new SharedPreferencesStorage(
                this,
                new JsonConverter(
                    new SimpleChoreSelectorConverter(),
                    new WeeklyEffortTrackerConverter()));
        }

        if (serena == null)
        {
            serena = new Serena(
                storage,
                new RealTimeService(),
                new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
                new SimpleChoreSelector());

            serena.load();

            globalState.setSerena(serena);
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

        RoutineNotifier.setupNotificationChannel(this);

        LocalDateTime nextRoutineProcedureTime = serena.getRoutineManager().getNextProcedureTime();

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
                else if (selectedFragment != R.id.nav_todays)
                {
                    goToFragment(R.id.nav_todays);

                    NavigationView navigationView = findViewById(R.id.nav_view);

                    navigationView.setCheckedItem(R.id.nav_todays);
                }
                else
                {
                    finish();
                }
            }
        });

        getSupportFragmentManager().registerFragmentLifecycleCallbacks(
            new FragmentManager.FragmentLifecycleCallbacks()
            {
                @Override
                public void onFragmentResumed(FragmentManager fm, Fragment f)
                {
                    super.onFragmentResumed(fm, f);

                    updateIconColors();
                }
            }, true
        );

        if (savedInstanceState == null)
        {
            goToFragment(R.id.nav_todays);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        updateIconColors();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        serena.save();
    }

    private void updateIconColors()
    {
        updateBalancerIconColor();
        updateSleepIconColor();
    }

    private void updateBalancerIconColor()
    {
        ImageView balancerImageView = findViewById(R.id.balancerImageView);

        BalancerManager balancerManager = serena.getBalancerManager();

        balancerImageView.setColorFilter(
            balancerManager.isAhead() ? Color.GREEN : Color.RED,
            PorterDuff.Mode.SRC_IN);
    }

    private void updateSleepIconColor()
    {
        ImageView sleepImageView = findViewById(R.id.sleepImageView);

        SleepManager sleepManager = serena.getSleepManager();

        sleepImageView.setColorFilter(
            sleepManager.isAhead() ? Color.GREEN : Color.RED,
            PorterDuff.Mode.SRC_IN);
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

        serena.load();
    }
}
