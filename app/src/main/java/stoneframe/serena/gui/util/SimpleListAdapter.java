package stoneframe.serena.gui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import stoneframe.serena.R;

public class SimpleListAdapter<T> extends BaseAdapter
{
    private final Context context;

    private final Supplier<List<T>> listFunction;
    private final Function<T, String> mainTextFunction;
    private final Function<T, String> secondaryTextFunction;
    private final Function<T, String> bottomTextFunction;
    private final Function<T, Integer> backgroundColorFunction;
    private final Function<T, Integer> borderColorFunction;

    SimpleListAdapter(
        @NonNull Context context,
        @NonNull Supplier<List<T>> listFunction,
        @NonNull Function<T, String> mainTextFunction,
        @Nullable Function<T, String> secondaryTextFunction,
        @Nullable Function<T, String> bottomTextFunction,
        @Nullable Function<T, Integer> backgroundColorFunction,
        @Nullable Function<T, Integer> borderColorFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.mainTextFunction = mainTextFunction;
        this.secondaryTextFunction = getOrDefault(secondaryTextFunction, x -> "");
        this.bottomTextFunction = getOrDefault(bottomTextFunction, x -> "");
        this.backgroundColorFunction = getOrDefault(
            backgroundColorFunction,
            x -> Color.TRANSPARENT);
        this.borderColorFunction = getOrDefault(borderColorFunction, x -> Color.TRANSPARENT);
    }

    @Override
    public int getCount()
    {
        return listFunction.get().size();
    }

    @Override
    public Object getItem(int position)
    {
        return listFunction.get().get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_simple, parent, false);
        }

        T item = listFunction.get().get(position);

        TextView mainTextView = convertView.findViewById(R.id.mainText);
        mainTextView.setText(mainTextFunction.apply(item));

        TextView secondaryTextView = convertView.findViewById(R.id.secondaryText);
        secondaryTextView.setText(secondaryTextFunction.apply(item));

        TextView bottomTextView = convertView.findViewById(R.id.bottomText);
        String bottomText = bottomTextFunction.apply(item);

        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setShape(GradientDrawable.RECTANGLE);
        borderDrawable.setCornerRadius(10);
        borderDrawable.setColor(backgroundColorFunction.apply(item));
        borderDrawable.setStroke(7, borderColorFunction.apply(item));

        convertView.setBackground(borderDrawable);

        if (bottomText.isEmpty())
        {
            bottomTextView.setVisibility(View.GONE);
        }
        else
        {
            bottomTextView.setVisibility(View.VISIBLE);
            bottomTextView.setText(bottomText);
        }

        return convertView;
    }

    public int getPosition(T item)
    {
        return listFunction.get().indexOf(item);
    }

    private static <T> T getOrDefault(T ori, T def)
    {
        return ori != null ? ori : def;
    }
}
