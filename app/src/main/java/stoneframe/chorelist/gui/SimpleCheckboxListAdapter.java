package stoneframe.chorelist.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimpleCheckboxListAdapter<T> extends BaseAdapter
{
    private final Context context;
    private final Supplier<List<T>> listFunction;
    private final Function<T, String> textFunction;

    public SimpleCheckboxListAdapter(
        @NonNull Context context,
        Supplier<List<T>> listFunction,
        Function<T, String> textFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.textFunction = textFunction;

        notifyDataSetChanged();
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
        return listFunction.get().get(position).hashCode();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Holder holder = new Holder();

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            CheckedTextView checkedTextView = (CheckedTextView)inflater.inflate(
                android.R.layout.simple_list_item_checked,
                parent,
                false);

            holder.checkedTextView = checkedTextView;

            convertView = checkedTextView;
        }
        else
        {
            holder = (Holder)convertView.getTag();
        }

        T item = (T)getItem(position);

        assert item != null;

        holder.checkedTextView.setText(textFunction.apply(item));
        holder.checkedTextView.setTag(holder);
        holder.item = item;

        return convertView;
    }

    private class Holder
    {
        public CheckedTextView checkedTextView;
        public T item;
    }
}
