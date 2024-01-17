package stoneframe.chorelist.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.function.Function;

public class SimpleCheckboxArrayAdapter<T> extends ArrayAdapter<T>
{
    private final Context context;
    private final Function<T, String> textFunction;
    private final Function<T, Boolean> isCheckedFunction;

    public SimpleCheckboxArrayAdapter(
        @NonNull Context context,
        Function<T, String> textFunction,
        Function<T, Boolean> isCheckedFunction)
    {
        super(context, android.R.layout.simple_list_item_checked);
        this.context = context;
        this.textFunction = textFunction;
        this.isCheckedFunction = isCheckedFunction;
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

        T item = getItem(position);

        assert item != null;

        holder.checkedTextView.setText(textFunction.apply(item));
        holder.checkedTextView.setChecked(isCheckedFunction.apply(item));
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
