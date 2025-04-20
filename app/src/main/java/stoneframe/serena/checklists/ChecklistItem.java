package stoneframe.serena.checklists;

public class ChecklistItem
{
    private String description;
    private boolean isChecked;

    public ChecklistItem(String description)
    {
        this.description = description;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public boolean isChecked()
    {
        return isChecked;
    }

    public void setChecked(boolean checked)
    {
        isChecked = checked;
    }
}
