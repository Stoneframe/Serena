package stoneframe.serena.model.routines;

import androidx.annotation.NonNull;

import java.util.List;

import stoneframe.serena.model.Editor;
import stoneframe.serena.model.timeservices.TimeService;

public abstract class RoutineEditor<TListener extends RoutineEditor.RoutineEditorListener> extends Editor<TListener>
{
    protected final RoutineManager routineManager;

    private final Routine<?> routine;

    private final PropertyUtil<String> nameProperty;
    private final PropertyUtil<Boolean> isEnabledProperty;

    protected RoutineEditor(
        RoutineManager routineManager,
        Routine<?> routine,
        TimeService timeService)
    {
        super(timeService);

        this.routineManager = routineManager;
        this.routine = routine;

        nameProperty = getNameProperty(routine);
        isEnabledProperty = getIsEnabledProperty(routine);
    }

    public String getName()
    {
        return nameProperty.getValue();
    }

    public void setName(String name)
    {
        nameProperty.setValue(name);
    }

    public boolean isEnabled()
    {
        return isEnabledProperty.getValue();
    }

    public void setEnabled(boolean isEnabled)
    {
        isEnabledProperty.setValue(isEnabled);
    }

    public List<Procedure> getAllProcedures()
    {
        return routine.getAllProcedures();
    }

    public void reset()
    {
        routine.reset(getNow());
    }

    public void save()
    {
        routine.save();

        if (!routineManager.containsRoutine(routine))
        {
            routineManager.addRoutine(routine);
        }
    }

    public void revert()
    {
        routine.revert();
    }

    public void remove()
    {
        if (routineManager.containsRoutine(routine))
        {
            routineManager.removeRoutine(routine);
        }
    }

    private @NonNull PropertyUtil<String> getNameProperty(Routine<?> routine)
    {
        return new PropertyUtil<>(
            routine::getName,
            routine::setName,
            v -> notifyListeners(RoutineEditorListener::nameChanged));
    }

    private @NonNull PropertyUtil<Boolean> getIsEnabledProperty(Routine<?> routine)
    {
        return new PropertyUtil<>(
            routine::isEnabled,
            routine::setEnabled,
            v -> notifyListeners(RoutineEditorListener::isEnabledChanged));
    }

    public interface RoutineEditorListener
    {
        void nameChanged();

        void isEnabledChanged();
    }
}
