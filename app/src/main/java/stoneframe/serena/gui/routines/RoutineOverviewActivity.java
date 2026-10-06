package stoneframe.serena.gui.routines;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.joda.time.LocalDate;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.notifications.Notifier;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.routines.ProcedureOccurrence;
import stoneframe.serena.routines.RoutineManager;

public class RoutineOverviewActivity extends AppCompatActivity
{
    private Button backButton;
    private Button forwardButton;
    private TextView dateTextView;

    private SimpleListAdapter<ProcedureOccurrence> daysProceduresListAdapter;
    private ListView daysProceduresList;

    private Button doneButton;

    private Serena serena;
    private RoutineManager routineManager;

    private LocalDate date;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        serena = GlobalState.getInstance().getOrLoadSerena();
        routineManager = serena.getRoutineManager();

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
            () -> routineManager.getProceduresForDate(date),
            ProcedureOccurrence::getDescription)
            .withSecondaryTextFunction(p -> p.getDateTime().toString("HH:mm"))
            .withBottomTextFunction(p -> p.getRoutine().getName())
            .withBackgroundColorFunction(this::getBackgroundColor)
            .withBorderColorFunction(this::getBorderColor)
            .create();

        daysProceduresList.setAdapter(daysProceduresListAdapter);
        daysProceduresList.setOnItemClickListener((parent, view, position, id) ->
            confirmProcedureCompletion((ProcedureOccurrence)daysProceduresListAdapter.getItem(position)));

        doneButton.setOnClickListener(v -> finish());
    }

    private void confirmProcedureCompletion(ProcedureOccurrence occurrence)
    {
        if (occurrence.isDone())
        {
            return;
        }

        DialogUtils.showConfirmationDialog(
            this,
            getString(R.string.routine_overview_complete_title),
            getString(
                R.string.routine_overview_complete_message,
                occurrence.getDescription(),
                occurrence.getRoutine().getName(),
                occurrence.getDateTime().toString("yyyy-MM-dd HH:mm")),
            isConfirmed ->
            {
                if (isConfirmed)
                {
                    completeProcedure(occurrence);
                }
            });
    }

    private void completeProcedure(ProcedureOccurrence occurrence)
    {
        routineManager.procedureDone(occurrence);
        serena.save();
        daysProceduresListAdapter.notifyDataSetChanged();
        Notifier.showRoutineNotification(this, serena, false);
    }

    private int getBackgroundColor(ProcedureOccurrence procedure)
    {
        return ContextCompat.getColor(
            this,
            procedure.isDone() ? R.color.status_success_container : R.color.surface);
    }

    private int getBorderColor(ProcedureOccurrence procedure)
    {
        return ContextCompat.getColor(
            this,
            procedure.isDone() ? R.color.status_success : R.color.outline);
    }
}
