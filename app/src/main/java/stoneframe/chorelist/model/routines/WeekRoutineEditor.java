package stoneframe.chorelist.model.routines;

import static stoneframe.chorelist.model.routines.WeekRoutineEditor.WeekRoutineEditorListener;

import stoneframe.chorelist.model.timeservices.TimeService;

public class WeekRoutineEditor extends RoutineEditor<WeekRoutineEditorListener>
{
    private final WeekRoutine weekRoutine;

    WeekRoutineEditor(
        RoutineManager routineManager,
        WeekRoutine weekRoutine,
        TimeService timeService)
    {
        super(routineManager, weekRoutine, timeService);

        this.weekRoutine = weekRoutine;
    }

    public Week getWeek()
    {
        return weekRoutine.getWeek();
    }

    public void addProcedure(int weekDay, Procedure procedure)
    {
        weekRoutine.getWeek().getWeekDay(weekDay).addProcedure(procedure);

        notifyListeners(WeekRoutineEditorListener::procedureAdded);
    }

    public void removeProcedure(int weekDay, Procedure procedure)
    {
        weekRoutine.getWeek().getWeekDay(weekDay).removeProcedure(procedure);

        notifyListeners(WeekRoutineEditorListener::procedureRemoved);
    }

    public interface WeekRoutineEditorListener extends RoutineEditorListener
    {
        void procedureAdded();

        void procedureRemoved();
    }
}
