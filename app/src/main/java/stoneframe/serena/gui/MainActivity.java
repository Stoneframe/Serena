package stoneframe.serena.gui;

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

import stoneframe.serena.R;
import stoneframe.serena.gui.checklists.AllChecklistsFragment;
import stoneframe.serena.gui.chores.AllChoresFragment;
import stoneframe.serena.gui.limiters.AllLimitersFragment;
import stoneframe.serena.gui.routines.AllRoutinesFragment;
import stoneframe.serena.gui.routines.RoutineNotifier;
import stoneframe.serena.gui.tasks.AllTasksFragment;
import stoneframe.serena.gui.today.TodayFragment;
import stoneframe.serena.json.ContainerJsonConverter;
import stoneframe.serena.json.SimpleChoreSelectorConverter;
import stoneframe.serena.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.Storage;
import stoneframe.serena.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.model.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.model.storages.JsonConverter;
import stoneframe.serena.model.storages.SharedPreferencesStorage;
import stoneframe.serena.model.timeservices.RealTimeService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private ActivityResultLauncher<Intent> editStorageLauncher;

    private Class<?> fragmentClass;

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

        navigationView.getMenu().getItem(0).setChecked(true);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

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

        serena.save();
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
