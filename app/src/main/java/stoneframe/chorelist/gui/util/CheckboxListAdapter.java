package stoneframe.chorelist.gui.util;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import stoneframe.chorelist.R;

/**
 * @noinspection unchecked
 */
public class CheckboxListAdapter<T> extends BaseAdapter
{
    private final Context context;

    private final Supplier<List<T>> listFunction;
    private final Function<T, String> mainTextFunction;
    private final Function<T, Boolean> isCheckedFunction;
    private final Function<T, String> bottomTextFunction;

    private CheckboxCheckedChangeListener<T> checkboxCheckedChangeListener;

    public CheckboxListAdapter(
        @NonNull Context context,
        Supplier<List<T>> listFunction,
        Function<T, String> mainTextFunction,
        Function<T, Boolean> isCheckedFunction,
        Function<T, String> bottomTextFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.mainTextFunction = mainTextFunction;
        this.isCheckedFunction = isCheckedFunction;
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

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Holder holder = new Holder();

        if (convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_check_box, parent, false);

            holder.mainTextView = convertView.findViewById(R.id.mainText);
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            holder.checkBox.setOnClickListener(this::onCheckBoxClicked);
            holder.bottomTextView = convertView.findViewById(R.id.bottomText);

            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder)convertView.getTag();
        }

        T item = listFunction.get().get(position);

        assert item != null;

        String mainText = mainTextFunction.apply(item);
        boolean isChecked = isCheckedFunction.apply(item);
        String bottomText = bottomTextFunction.apply(item);

        holder.mainTextView.setText(mainText);
        holder.mainTextView.setTextColor(isChecked ? Color.LTGRAY : Color.BLACK);
        holder.checkBox.setChecked(isChecked);
        holder.checkBox.setTag(holder);

        if (bottomText.isEmpty())
        {
            holder.bottomTextView.setVisibility(View.GONE);
        }
        else
        {
            holder.bottomTextView.setVisibility(View.VISIBLE);
            holder.bottomTextView.setText(bottomText);
        }

        holder.item = item;

        return convertView;
    }

    public void setCheckboxChangedListener(CheckboxCheckedChangeListener<T> listener)
    {
        this.checkboxCheckedChangeListener = listener;
    }

    private void onCheckBoxClicked(View view)
    {
        Holder holder = (Holder)view.getTag();

        holder.mainTextView.setTextColor(isCheckedFunction.apply(holder.item) ? Color.LTGRAY : Color.BLACK);

        notifyCheckboxCheckedChanged(holder);
        notifyDataSetChanged();
    }

    private void notifyCheckboxCheckedChanged(Holder holder)
    {
        if (checkboxCheckedChangeListener != null)
        {
            checkboxCheckedChangeListener.onCheckboxChanged(
                holder.item,
                holder.checkBox.isChecked());
        }
    }

    private class Holder
    {
        public TextView mainTextView;
        public CheckBox checkBox;
        public TextView bottomTextView;
        public T item;
    }

    public interface CheckboxCheckedChangeListener<T>
    {
        void onCheckboxChanged(T item, boolean isChecked);
    }
}