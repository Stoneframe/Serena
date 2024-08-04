package stoneframe.chorelist.gui.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleListAdapter<T> extends BaseAdapter
{
    private final Context context;

    private final Supplier<List<T>> listFunction;
    private final Function<T, String> textFunction;

    public SimpleListAdapter(
        Context context,
        Supplier<List<T>> listFunction,
        Function<T, String> textFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.textFunction = textFunction;
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
            convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        T item = listFunction.get().get(position);

        TextView textView = convertView.findViewById(android.R.id.text1);
        textView.setText(textFunction.apply(item));

        return convertView;
    }
}
