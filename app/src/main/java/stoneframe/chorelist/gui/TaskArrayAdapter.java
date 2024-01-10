package stoneframe.chorelist.gui;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Task;

public class TaskArrayAdapter extends ArrayAdapter<Task>
{
    private final Context context;

    public TaskArrayAdapter(@NonNull Context context)
    {
        super(context, R.layout.list_item_check_box);
        this.context = context;
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

            holder.textView = convertView.findViewById(R.id.txtName);
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            holder.checkBox.setOnClickListener(this::onCheckBoxClicked);

            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder)convertView.getTag();
        }

        Task item = getItem(position);

        assert item != null;

        holder.textView.setText(item.getDescription());
        holder.textView.setTextColor(item.isDone() ? Color.LTGRAY : Color.BLACK);
        holder.checkBox.setChecked(item.isDone());
        holder.checkBox.setTag(holder);
        holder.item = item;

        return convertView;
    }

    private void onCheckBoxClicked(View view)
    {
        Holder holder = (Holder)view.getTag();

        holder.item.setDone(holder.checkBox.isChecked());
        holder.textView.setTextColor(holder.item.isDone() ? Color.LTGRAY : Color.BLACK);

        notifyDataSetChanged();
    }

    private static class Holder
    {
        public CheckBox checkBox;
        public TextView textView;
        public Task item;
    }
}