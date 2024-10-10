package stoneframe.chorelist.gui.routines.days;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.routines.RoutineActivity;
import stoneframe.chorelist.model.routines.DayRoutine;
import stoneframe.chorelist.model.routines.Procedure;

public class DayRoutineActivity extends RoutineActivity<DayRoutine>
{
    private ListView procedureListView;
    private ArrayAdapter<Procedure> procedureListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setTitle("Day Routine");

        procedureListAdapter = new ArrayAdapter<>(
            getBaseContext(),
            android.R.layout.simple_list_item_1);

        procedureListAdapter.addAll(routine.getAllProcedures());

        procedureListView = findViewById(R.id.procedures);
        procedureListView.setAdapter(procedureListAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
            editProcedure(position));
        procedureListView.setOnItemLongClickListener((parent, view, position, id) ->
            removeOrCopyProcedure(position));
    }

    @Override
    protected int getRoutineContentView()
    {
        return R.layout.activity_routine_day;
    }

    @Override
    protected void addProcedure()
    {
        DayProcedureEditDialog.create(this, procedure ->
        {
            routine.addProcedure(procedure);

            procedureListAdapter.add(procedure);
            procedureListAdapter.sort(Procedure::compareTo);
        });
    }

    private void editProcedure(int position)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

        assert procedure != null;

        DayProcedureEditDialog.edit(
            this,
            procedure,
            editedProcedure ->
            {
                routine.removeProcedure(procedure);
                routine.addProcedure(editedProcedure);

                procedureListAdapter.clear();
                procedureListAdapter.addAll(routine.getAllProcedures());
            });
    }

    private boolean removeOrCopyProcedure(int position)
    {
        Procedure procedure = procedureListAdapter.getItem(position);

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
        routine.removeProcedure(procedure);
        procedureListAdapter.remove(procedure);
    }

    private void copyProcedure(Procedure procedure)
    {
        DayProcedureEditDialog.copy(
            this,
            procedure,
            copiedProcedure ->
            {
                routine.addProcedure(copiedProcedure);

                procedureListAdapter.add(copiedProcedure);
                procedureListAdapter.sort(Procedure::compareTo);
            });
    }
}
