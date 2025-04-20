package stoneframe.serena.gui.routines;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.routines.Procedure;
import stoneframe.serena.routines.Routine;
import stoneframe.serena.routines.RoutineManager;

public class RoutineOverviewActivity extends AppCompatActivity
{
    private Button backButton;
    private Button forwardButton;
    private TextView dateTextView;

    private SimpleListAdapter<RoutineProcedureLink> daysProceduresListAdapter;
    private ListView daysProceduresList;

    private Button doneButton;

    private RoutineManager routineManager;

    private LocalDate date;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        routineManager = GlobalState.getInstance().getSerena().getRoutineManager();

        date = LocalDate.now();

        setContentView(R.layout.activity_routine_overview);
        setTitle("Routine Overview");

        backButton = findViewById(R.id.backButton);
        forwardButton = findViewById(R.id.forwardButton);
        dateTextView = findViewById(R.id.dateTextView);

        daysProceduresList = findViewById(R.id.daysProceduresList);

        doneButton = findViewById(R.id.doneButton);

        backButton.setOnClickListener(v ->
        {
            date = date.minusDays(1);
            dateTextView.setText(date.toString("yyyy-MM-dd"));
            daysProceduresListAdapter.notifyDataSetChanged();
        });

        forwardButton.setOnClickListener(v ->
        {
            date = date.plusDays(1);
            dateTextView.setText(date.toString("yyyy-MM-dd"));
            daysProceduresListAdapter.notifyDataSetChanged();
        });

        dateTextView.setText(date.toString("yyyy-MM-dd"));

        daysProceduresListAdapter = new SimpleListAdapterBuilder<>(
            this,
            this::getRoutineProcedures,
            p -> p.getProcedure().getDescription())
            .withSecondaryTextFunction(p -> p.getProcedure().getTime().toString("HH:mm"))
            .withBottomTextFunction(p -> p.getRoutine().getName())
            .withBackgroundColorFunction(RoutineOverviewActivity::getBackgroundColor)
            .withBorderColorFunction(RoutineOverviewActivity::getBorderColor)
            .create();

        daysProceduresList.setAdapter(daysProceduresListAdapter);

        doneButton.setOnClickListener(v -> finish());
    }

    private List<RoutineProcedureLink> getRoutineProcedures()
    {
        return routineManager.getProceduresForDate(date)
            .stream()
            .map(p -> new RoutineProcedureLink(p.second, p.first))
            .collect(Collectors.toList());
    }

    private static int getBackgroundColor(RoutineProcedureLink link)
    {
        return link.isDone() ? Color.parseColor("#bdfaa7") : Color.parseColor("#edece6");
    }

    private static int getBorderColor(RoutineProcedureLink link)
    {
        return link.isDone() ? Color.parseColor("#42f700") : Color.parseColor("#6e6e6e");
    }

    public class RoutineProcedureLink implements Comparable<RoutineProcedureLink>
    {
        private final Routine<?> routine;
        private final Procedure procedure;

        public RoutineProcedureLink(Routine<?> routine, Procedure procedure)
        {
            this.routine = routine;
            this.procedure = procedure;
        }

        public Routine<?> getRoutine()
        {
            return routine;
        }

        public Procedure getProcedure()
        {
            return procedure;
        }

        public boolean isDone()
        {
            return routine.isDone(date, procedure);
        }

        @Override
        public int compareTo(RoutineProcedureLink other)
        {
            return this.procedure.getTime().compareTo(other.procedure.getTime());
        }
    }
}