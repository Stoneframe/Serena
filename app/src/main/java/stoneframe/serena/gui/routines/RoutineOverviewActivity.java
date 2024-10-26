package stoneframe.serena.gui.routines;

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
import stoneframe.serena.model.routines.Procedure;
import stoneframe.serena.model.routines.Routine;
import stoneframe.serena.model.routines.RoutineManager;

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

        daysProceduresListAdapter = new SimpleListAdapter<>(
            this,
            this::getRoutineProcedures,
            p -> p.getProcedure().getDescription(),
            p -> p.getProcedure().getTime().toString("HH:mm"),
            p -> p.getRoutine().getName());

        daysProceduresList.setAdapter(daysProceduresListAdapter);

        doneButton.setOnClickListener(v -> finish());
    }

    private List<RoutineProcedureLink> getRoutineProcedures()
    {
        return routineManager.getAllRoutines().stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> r.getProceduresForDate(date)
                .stream()
                .map(p -> new RoutineProcedureLink(r, p)))
            .sorted()
            .collect(Collectors.toList());
    }

    public static class RoutineProcedureLink implements Comparable<RoutineProcedureLink>
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

        @Override
        public int compareTo(RoutineProcedureLink other)
        {
            return this.procedure.getTime().compareTo(other.procedure.getTime());
        }
    }
}