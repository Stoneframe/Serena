package stoneframe.serena.model.routines;

import static stoneframe.serena.model.routines.FortnightRoutineEditor.FortnightRoutineEditorListener;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import stoneframe.serena.model.timeservices.TimeService;

public class FortnightRoutineEditor extends RoutineEditor<FortnightRoutineEditorListener>
{
    private final FortnightRoutine fortnightRoutine;

    private final PropertyUtil<LocalDate> startDateProperty;

    FortnightRoutineEditor(
        RoutineManager routineManager,
        FortnightRoutine fortnightRoutine,
        TimeService timeService)
    {
        super(routineManager, fortnightRoutine, timeService);

        this.fortnightRoutine = fortnightRoutine;
        this.fortnightRoutine.edit();

        startDateProperty = getStartDateProperty(fortnightRoutine);
    }

    public LocalDate getStartDate()
    {
        return startDateProperty.getValue();
    }

    public void setStartDate(LocalDate startDate)
    {
        startDateProperty.setValue(startDate);
    }

    public Week getWeek1()
    {
        return fortnightRoutine.getWeek1();
    }

    public Week getWeek2()
    {
        return fortnightRoutine.getWeek2();
    }

    public void addProcedure(int week, int weekDay, Procedure procedure)
    {
        fortnightRoutine.getWeek(week).getWeekDay(weekDay).addProcedure(procedure);

        notifyListeners(FortnightRoutineEditorListener::procedureAdded);
    }

    public void removeProcedure(int week, int weekDay, Procedure procedure)
    {
        fortnightRoutine.getWeek(week).getWeekDay(weekDay).removeProcedure(procedure);

        notifyListeners(FortnightRoutineEditorListener::procedureRemoved);
    }

    private @NonNull PropertyUtil<LocalDate> getStartDateProperty(FortnightRoutine fortnightRoutine)
    {
        return new PropertyUtil<>(
            fortnightRoutine::getStartDate,
            fortnightRoutine::setStartDate,
            v -> notifyListeners(FortnightRoutineEditorListener::startDateChanged));
    }

    public interface FortnightRoutineEditorListener extends RoutineEditorListener
    {
        void startDateChanged();

        void procedureAdded();

        void procedureRemoved();
    }
}
