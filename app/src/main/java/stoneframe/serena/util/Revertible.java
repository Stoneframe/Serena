package stoneframe.serena.util;

public abstract class Revertible<T>
{
    private T data;

    private transient T checkpoint;

    protected Revertible(T data)
    {
        this.data = data;
    }

    public void edit()
    {
        save();
    }

    public void save()
    {
        checkpoint = DeepCopy.copy(data);
    }

    public void revert()
    {
        data = checkpoint;
    }

    protected T data()
    {
        return data;
    }
}
