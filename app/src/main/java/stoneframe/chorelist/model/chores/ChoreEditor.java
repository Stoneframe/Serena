package stoneframe.chorelist.model.chores;

import androidx.annotation.NonNull;

import stoneframe.chorelist.model.Editor;
import stoneframe.chorelist.model.timeservices.TimeService;

public class ChoreEditor extends Editor<ChoreEditor.ChoreEditorListener>
{
    private final ChoreManager choreManager;

    private final Chore chore;

    private final PropertyUtil<Boolean> isEnabledProperty;
    private final PropertyUtil<String> descriptionProperty;
    private final PropertyUtil<Integer> priorityProperty;
    private final PropertyUtil<Integer> effortProperty;
    private final PropertyUtil<Integer> intervalUnitProperty;
    private final PropertyUtil<Integer> intervalLengthProperty;

    public ChoreEditor(ChoreManager choreManager, Chore chore, TimeService timeService)
    {
        super(timeService);

        this.choreManager = choreManager;
        this.chore = chore;

        isEnabledProperty = getIsEnabledProperty();
        descriptionProperty = getDescriptionProperty();
        priorityProperty = getPriorityProperty();
        effortProperty = getEffortProperty();
        intervalUnitProperty = getIntervalUnitProperty();
        intervalLengthProperty = getIntervalLengthProperty();
    }

    public boolean isEnabled()
    {
        return isEnabledProperty.getValue();
    }

    public void setEnabled(boolean isEnabled)
    {
        isEnabledProperty.setValue(isEnabled);
    }

    public String getDescription()
    {
        return descriptionProperty.getValue();
    }

    public void setDescription(String description)
    {
        descriptionProperty.setValue(description);
    }

    public int getPriority()
    {
        return priorityProperty.getValue();
    }

    public void setPriority(int priority)
    {
        priorityProperty.setValue(priority);
    }

    public int getEffort()
    {
        return effortProperty.getValue();
    }

    public void setEffort(int effort)
    {
        effortProperty.setValue(effort);
    }

    public int getIntervalUnit()
    {
        return intervalUnitProperty.getValue();
    }

    public void setIntervalUnit(int intervalUnit)
    {
        intervalUnitProperty.setValue(intervalUnit);
    }

    public int getIntervalLength()
    {
        return intervalLengthProperty.getValue();
    }

    public void setIntervalLength(int intervalLength)
    {
        intervalLengthProperty.setValue(intervalLength);
    }

    public void save()
    {
        if (!choreManager.containsChore(chore))
        {
            choreManager.addChore(chore);
        }
    }

    public void remove()
    {
        if (choreManager.containsChore(chore))
        {
            choreManager.removeChore(chore);
        }
    }

    private @NonNull PropertyUtil<Boolean> getIsEnabledProperty()
    {
        return new PropertyUtil<>(
            chore::isEnabled,
            chore::setEnabled,
            v -> notifyListeners(ChoreEditorListener::isEnabledChanged));
    }

    private @NonNull PropertyUtil<String> getDescriptionProperty()
    {
        return new PropertyUtil<>(
            chore::getDescription,
            chore::setDescription,
            v -> notifyListeners(ChoreEditorListener::descriptionChanged));
    }

    private @NonNull PropertyUtil<Integer> getPriorityProperty()
    {
        return new PropertyUtil<>(
            chore::getPriority,
            chore::setPriority,
            v -> notifyListeners(ChoreEditorListener::priorityChanged));
    }

    private @NonNull PropertyUtil<Integer> getEffortProperty()
    {
        return new PropertyUtil<>(
            chore::getEffort,
            chore::setEffort,
            v -> notifyListeners(ChoreEditorListener::effortChanged));
    }

    private @NonNull PropertyUtil<Integer> getIntervalUnitProperty()
    {
        return new PropertyUtil<>(
            chore::getIntervalUnit,
            chore::setIntervalUnit,
            v -> notifyListeners(ChoreEditorListener::intervalUnitChanged));
    }

    private @NonNull PropertyUtil<Integer> getIntervalLengthProperty()
    {
        return new PropertyUtil<>(
            chore::getIntervalLength,
            chore::setIntervalLength,
            v -> notifyListeners(ChoreEditorListener::intervalLengthChanged));
    }

    public Repetition getRepetition()
    {
        return chore.getRepetition();
    }

    public interface ChoreEditorListener
    {
        void isEnabledChanged();

        void descriptionChanged();

        void priorityChanged();

        void effortChanged();

        void intervalUnitChanged();

        void intervalLengthChanged();
    }
}
