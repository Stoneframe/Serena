package stoneframe.serena.gui.routines.days;

import android.app.AlertDialog;
import android.widget.ListView;

import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.routines.EditRoutineActivity;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.routines.DayRoutine;
import stoneframe.serena.routines.DayRoutineEditor;
import stoneframe.serena.routines.Procedure;

public class DayRoutineActivity extends EditRoutineActivity<DayRoutine, DayRoutineEditor> implements DayRoutineEditor.DayRoutineEditorListener
{
    private SimpleListAdapter<Procedure> procedureListAdapter;
    private ListView procedureListView;

    @Override
    public void nameChanged()
    {

    }

    @Override
    public void isEnabledChanged()
    {

    }

    @Override
    public void procedureAdded()
    {
        procedureListAdapter.notifyDataSetChanged();
    }

    @Override
    public void procedureRemoved()
    {
        procedureListAdapter.notifyDataSetChanged();
    }

    @Override
    protected String getActivityTitle()
    {
        return "Day Routine";
    }

    @Override
    protected DayRoutineEditor getRoutineEditor(DayRoutine routine)
    {
        return serena.getRoutineManager().getDayRoutineEditor(routine);
    }

    @Override
    protected void createSpecialisedActivity()
    {
        procedureListAdapter = new SimpleListAdapterBuilder<>(
            this,
            () -> routineEditor.getAllProcedures().stream().sorted().collect(Collectors.toList()),
            Procedure::toString)
            .create();

        procedureListView = findViewById(R.id.procedures);
        procedureListView.setAdapter(procedureListAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
            editProcedure(position));
        procedureListView.setOnItemLongClickListener((parent, view, position, id) ->
            removeOrCopyProcedure(position));
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        routineEditor.addListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        routineEditor.removeListener(this);
    }

    @Override
    protected int getRoutineContentView()
    {
        return R.layout.activity_routine_day;
    }

    @Override
    protected void addProcedure()
    {
        DayProcedureEditDialog.create(this, procedure -> routineEditor.addProcedure(procedure));
    }

    private void editProcedure(int position)
    {
        Procedure procedure = (Procedure)procedureListAdapter.getItem(position);

        assert procedure != null;

        DayProcedureEditDialog.edit(
            this,
            procedure,
            editedProcedure ->
            {
                routineEditor.removeProcedure(procedure);
                routineEditor.addProcedure(editedProcedure);
            });
    }

    private boolean removeOrCopyProcedure(int position)
    {
        Procedure procedure = (Procedure)procedureListAdapter.getItem(position);

        assert procedure != null;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Select option")
            .setCancelable(false)
            .setPositiveButton("Remove", (dialog, removeButtonId) ->
                removeProcedure(procedure))
            .setNegativeButton("Copy", (dialog, copyButtonId) ->
                copyProcedure(procedure))
            .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();

        return true;
    }

    private void removeProcedure(Procedure procedure)
    {
        routineEditor.removeProcedure(procedure);
    }

    private void copyProcedure(Procedure procedure)
    {
        DayProcedureEditDialog.copy(
            this,
            procedure,
            copiedProcedure -> routineEditor.addProcedure(copiedProcedure));
    }
}
