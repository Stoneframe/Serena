package stoneframe.serena;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import stoneframe.serena.timeservices.TimeService;

public abstract class Editor<Listener>
{
    private final List<Listener> listeners = new LinkedList<>();

    private final TimeService timeService;

    protected Editor(TimeService timeService) {this.timeService = timeService;}

    public void addListener(Listener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(Listener listener)
    {
        listeners.remove(listener);
    }

    protected void notifyListeners(Consumer<Listener> action)
    {
        listeners.forEach(action);
    }

    protected LocalDate getToday()
    {
        return timeService.getToday();
    }

    protected LocalDateTime getNow()
    {
        return timeService.getNow();
    }

    protected static class PropertyUtil<TProperty>
    {
        private final Supplier<TProperty> getValue;
        private final Consumer<TProperty> setValue;

        private final Consumer<TProperty> onChanged;

        public PropertyUtil(
            Supplier<TProperty> getValue,
            Consumer<TProperty> setValue,
            Consumer<TProperty> changed)
        {
            this.getValue = getValue;
            this.setValue = setValue;
            this.onChanged = changed;
        }

        public TProperty getValue()
        {
            return getValue.get();
        }

        public void setValue(TProperty value)
        {
            if (value != getValue.get())
            {
                setValue.accept(value);
                onChanged.accept(value);
            }
        }
    }
}
