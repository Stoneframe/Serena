package stoneframe.serena.gui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.balancers.BalancerManager;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.gui.balancers.AllBalancersFragment;
import stoneframe.serena.gui.checklists.AllChecklistsFragment;
import stoneframe.serena.gui.chores.AllChoresFragment;
import stoneframe.serena.gui.notes.AllNotesFragment;
import stoneframe.serena.gui.notifications.Notifier;
import stoneframe.serena.gui.reminders.AllRemindersFragment;
import stoneframe.serena.gui.routines.AllRoutinesFragment;
import stoneframe.serena.gui.sleep.SleepFragment;
import stoneframe.serena.gui.tasks.AllTasksFragment;
import stoneframe.serena.gui.today.TodayFragment;
import stoneframe.serena.sleep.SleepManager;
import stoneframe.serena.storages.JsonConverter;
import stoneframe.serena.storages.SharedPreferencesStorage;
import stoneframe.serena.storages.json.ContainerJsonConverter;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.timeservices.RealTimeService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final Map<Integer, Frag> fragments = new HashMap<Integer, Frag>()
    {{
        put(R.id.nav_todays, new Frag(TodayFragment.class, "Today"));
        put(R.id.nav_all_routines, new Frag(AllRoutinesFragment.class, "Routines"));
        put(R.id.nav_all_chores, new Frag(AllChoresFragment.class, "Chores"));
        put(R.id.nav_all_tasks, new Frag(AllTasksFragment.class, "Tasks"));
        put(R.id.nav_all_reminders, new Frag(AllRemindersFragment.class, "Reminders"));
        put(R.id.nav_all_checklists, new Frag(AllChecklistsFragment.class, "Checklists"));
        put(R.id.nav_balancers, new Frag(AllBalancersFragment.class, "Balancers"));
        put(R.id.nav_sleep, new Frag(SleepFragment.class, "Sleep"));
        put(R.id.nav_notes, new Frag(AllNotesFragment.class, "Notes"));
    }};

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

    public void updateSleepIconColor()
    {
        ImageView sleepImageView = findViewById(R.id.sleepImageView);

        SleepManager sleepManager = serena.getSleepManager();

        sleepImageView.setVisibility(sleepManager.isEnabled() ? View.VISIBLE : View.GONE);

        sleepImageView.setColorFilter(
            sleepManager.isAhead() ? Color.GREEN : Color.RED,
            PorterDuff.Mode.SRC_IN);
    }

    @SuppressLint("NonConstantResourceId")
    private boolean goToFragment(int fragmentId)
    {
        selectedFragment = fragmentId;

        try
        {
            NavigationView navigationView = findViewById(R.id.nav_view);
            navigationView.setCheckedItem(fragmentId);

            Class<?> clazz = getFragment(fragmentId).getClazz();

            Fragment fragment = (Fragment)clazz.newInstance();

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);

            setTitle(getFragment(fragmentId).getName());
        }
        catch (Exception e)
        {
            return false;
        }

        return true;
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

        Notifier.setupNotificationChannels(this);
        Notifier.scheduleAlarm(this, serena);

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
                public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f)
                {
                    super.onFragmentResumed(fm, f);

                    updateIconColors();
                }
            }, true
        );

        if (handleIntent(getIntent()))
        {
            return;
        }

        if (savedInstanceState == null)
        {
            goToFragment(R.id.nav_todays);
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
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

    private boolean handleIntent(Intent intent)
    {
        if (intent == null || !intent.hasExtra("fragment"))
        {
            return false;
        }

        int fragmentId = intent.getIntExtra("fragment", R.id.nav_todays);

        goToFragment(fragmentId);

        return true;
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

    private static Frag getFragment(int fragmentId)
    {
        if (!fragments.containsKey(fragmentId))
        {
            return fragments.get(R.id.nav_todays);
        }

        return fragments.get(fragmentId);
    }

    private static class Frag
    {
        private final Class<?> clazz;
        private final String name;

        public Frag(Class<?> clazz, String name)
        {
            this.clazz = clazz;
            this.name = name;
        }

        public Class<?> getClazz()
        {
            return clazz;
        }

        public String getName()
        {
            return name;
        }
    }
}
