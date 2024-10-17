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

    public ChoreEditor(ChoreManager choreManager, Chore chore, TimeService timeService)
    {
        super(timeService);

        this.choreManager = choreManager;
        this.chore = chore;
        this.chore.edit();

        isEnabledProperty = getIsEnabledProperty();
        descriptionProperty = getDescriptionProperty();
        priorityProperty = getPriorityProperty();
        effortProperty = getEffortProperty();
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

    public void save()
    {
        if (!choreManager.containsChore(chore))
        {
            choreManager.addChore(chore);
        }
    }

    public void revert()
    {
        chore.revert();
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

    public Repetition getRepetition()
    {
        return chore.getRepetition();
    }

    public void setRepetitionType(int repetitionType)
    {
        chore.setRepetitionType(repetitionType);
    }

    public void updateNext()
    {
        chore.getRepetition().updateNext(getToday());
    }

    public interface ChoreEditorListener
    {
        void isEnabledChanged();

        void descriptionChanged();

        void priorityChanged();

        void effortChanged();
    }
}
