package stoneframe.serena.gui.util;

import android.content.Context;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleListAdapterBuilder<T>
{
    private final Context context;
    private final Supplier<List<T>> listFunction;
    private final Function<T, String> mainTextFunction;

    private Function<T, String> secondaryTextFunction;
    private Function<T, String> bottomTextFunction;

    private Function<T, Integer> backgroundColorFunction;
    private Function<T, Integer> borderColorFunction;

    public SimpleListAdapterBuilder(
        Context context,
        Supplier<List<T>> listFunction,
        Function<T, String> mainTextFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.mainTextFunction = mainTextFunction;
    }

    public SimpleListAdapterBuilder<T> withSecondaryTextFunction(Function<T, String> secondaryTextFunction)
    {
        this.secondaryTextFunction = secondaryTextFunction;

        return this;
    }

    public SimpleListAdapterBuilder<T> withBottomTextFunction(Function<T, String> bottomTextFunction)
    {
        this.bottomTextFunction = bottomTextFunction;

        return this;
    }

    public SimpleListAdapterBuilder<T> withBackgroundColorFunction(Function<T, Integer> backgroundColorFunction)
    {
        this.backgroundColorFunction = backgroundColorFunction;

        return this;
    }

    public SimpleListAdapterBuilder<T> withBorderColorFunction(Function<T, Integer> borderColorFunction)
    {
        this.borderColorFunction = borderColorFunction;

        return this;
    }

    public SimpleListAdapter<T> create()
    {
        return new SimpleListAdapter<>(
            context,
            listFunction,
            mainTextFunction,
            secondaryTextFunction,
            bottomTextFunction,
            backgroundColorFunction,
            borderColorFunction);
    }
}
