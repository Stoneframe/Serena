package stoneframe.serena.gui.routines.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.function.Supplier;

import stoneframe.serena.R;
import stoneframe.serena.routines.Procedure;

public class ProcedureListAdapter extends BaseAdapter
{
    private final Context context;
    private final Supplier<List<Procedure>> listFunction;

    public ProcedureListAdapter(
        @NonNull Context context,
        @NonNull Supplier<List<Procedure>> listFunction)
    {
        this.context = context;
        this.listFunction = listFunction;
    }

    @Override
    public int getCount()
    {
        return listFunction.get().size();
    }

    @Override
    public Procedure getItem(int position)
    {
        return listFunction.get().get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = LayoutInflater.from(context).inflate(
                R.layout.expandable_list_item,
                parent,
                false);
        }

        Procedure procedure = getItem(position);
        TextView procedureTextView = convertView.findViewById(R.id.expandedListItem);
        procedureTextView.setText(String.format(
            "%s - %s",
            procedure.getTime().toString("HH.mm"),
            procedure.getDescription()));

        return convertView;
    }
}
