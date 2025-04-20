package stoneframe.serena.routines;

import static stoneframe.serena.routines.DayRoutineEditor.DayRoutineEditorListener;

import stoneframe.serena.timeservices.TimeService;

public class DayRoutineEditor extends RoutineEditor<DayRoutineEditorListener>
{
    private final DayRoutine routine;

    DayRoutineEditor(
        RoutineManager routineManager,
        DayRoutine routine,
        TimeService timeService)
    {
        super(routineManager, routine, timeService);

        this.routine = routine;
        this.routine.edit();
    }

    public void addProcedure(Procedure procedure)
    {
        routine.addProcedure(procedure);
        notifyListeners(DayRoutineEditorListener::procedureAdded);
    }

    public void removeProcedure(Procedure procedure)
    {
        routine.removeProcedure(procedure);
        notifyListeners(DayRoutineEditorListener::procedureRemoved);
    }

    public interface DayRoutineEditorListener extends RoutineEditorListener
    {
        void procedureAdded();

        void procedureRemoved();
    }
}
