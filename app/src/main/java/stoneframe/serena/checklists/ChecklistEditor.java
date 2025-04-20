package stoneframe.serena.checklists;

import androidx.annotation.NonNull;

import java.util.List;

import stoneframe.serena.Editor;
import stoneframe.serena.timeservices.TimeService;

public class ChecklistEditor extends Editor<ChecklistEditor.ChecklistEditorListener>
{
    private final ChecklistManager checklistManager;

    private final Checklist checklist;

    private final PropertyUtil<String> nameProperty;

    public ChecklistEditor(
        ChecklistManager checklistManager,
        Checklist checklist,
        TimeService timeService)
    {
        super(timeService);

        this.checklistManager = checklistManager;
        this.checklist = checklist;

        this.checklist.edit();

        nameProperty = getNameProperty(checklist);
    }

    public String getName()
    {
        return nameProperty.getValue();
    }

    public void setName(String name)
    {
        nameProperty.setValue(name);
    }

    public List<ChecklistItem> getItems()
    {
        return checklist.getItems();
    }

    public void addItem(ChecklistItem item)
    {
        checklist.addItem(item);

        int position = checklist.getItems().indexOf(item);

        notifyListeners(l -> l.checklistItemAdded(position, item));
    }

    public void removeItem(ChecklistItem item)
    {
        int position = checklist.getItems().indexOf(item);

        checklist.removeItem(item);

        notifyListeners(l -> l.checklistItemRemoved(position, item));
    }

    public void moveItem(ChecklistItem item, int newPosition)
    {
        int oldPosition = checklist.getItems().indexOf(item);

        checklist.moveItem(item, newPosition);

        notifyListeners(l -> l.checklistItemMoved(oldPosition, newPosition, item));
    }

    public void save()
    {
        checklist.save();
    }

    public void revert()
    {
        checklist.revert();
    }

    public void remove()
    {
        checklistManager.removeChecklist(checklist);
    }

    private @NonNull PropertyUtil<String> getNameProperty(Checklist checklist)
    {
        return new PropertyUtil<>(
            checklist::getName,
            checklist::setName,
            v -> notifyListeners(ChecklistEditorListener::nameChanged));
    }

    public interface ChecklistEditorListener
    {
        void nameChanged();

        void checklistItemAdded(int position, ChecklistItem item);

        void checklistItemRemoved(int position, ChecklistItem item);

        void checklistItemMoved(int oldPosition, int newPosition, ChecklistItem item);
    }
}
