package stoneframe.serena.gui.routines;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
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

        daysProceduresListAdapter = new SimpleListAdapterBuilder<>(
            this,
            this::getRoutineProcedures,
            p -> p.getProcedure().getDescription())
            .withSecondaryTextFunction(p -> p.getProcedure().getTime().toString("HH:mm"))
            .withBottomTextFunction(p -> p.getRoutine().getName())
            .withBackgroundColorFunction(p -> getBackgroundColor(p.getProcedure()))
            .withBorderColorFunction(p -> getBorderColor(p.getProcedure()))
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

    private static int getBackgroundColor(Procedure procedure)
    {
        if (isBetweenHours(procedure.getTime(), 0, 8)) return Color.parseColor("#edece6");
        if (isBetweenHours(procedure.getTime(), 8, 12)) return Color.parseColor("#fafa9d");
        if (isBetweenHours(procedure.getTime(), 12, 17)) return Color.parseColor("#fae1aa");
        if (isBetweenHours(procedure.getTime(), 17, 21)) return Color.parseColor("#c2caff");
        if (isBetweenHours(procedure.getTime(), 21, 24)) return Color.parseColor("#edece6");

        return Color.WHITE;
    }

    private static int getBorderColor(Procedure procedure)
    {
        if (isBetweenHours(procedure.getTime(), 0, 8)) return Color.parseColor("#6e6e6e");
        if (isBetweenHours(procedure.getTime(), 8, 12)) return Color.parseColor("#f7df05");
        if (isBetweenHours(procedure.getTime(), 12, 17)) return Color.parseColor("#f7b705");
        if (isBetweenHours(procedure.getTime(), 17, 21)) return Color.parseColor("#052df7");
        if (isBetweenHours(procedure.getTime(), 21, 24)) return Color.parseColor("#6e6e6e");

        return Color.WHITE;
    }

    private static boolean isBetweenHours(LocalTime time, int start, int end)
    {
        LocalTime startTime = new LocalTime(start, 0);
        LocalTime endTime = end == 24
            ? new LocalTime(23, 59)
            : new LocalTime(end, 0);

        return (startTime.isBefore(time) || startTime.isEqual(time))
            && endTime.isAfter(time);
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