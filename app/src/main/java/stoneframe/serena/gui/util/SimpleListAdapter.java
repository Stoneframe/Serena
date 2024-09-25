package stoneframe.serena.gui.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

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

    public SimpleListAdapter(
        Context context,
        Supplier<List<T>> listFunction,
        Function<T, String> mainTextFunction,
        Function<T, String> secondaryTextFunction,
        Function<T, String> bottomTextFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.mainTextFunction = mainTextFunction;
        this.secondaryTextFunction = secondaryTextFunction;
        this.bottomTextFunction = bottomTextFunction;
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
}
