package stoneframe.chorelist.gui.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import stoneframe.chorelist.R;

public class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>
{
    private final Supplier<List<T>> getList;
    private final Function<T, String> getText;

    public RecyclerAdapter(
        Supplier<List<T>> getList,
        Function<T, String> getText)
    {
        this.getList = getList;
        this.getText = getText;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_checklist_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        T item = getList.get().get(position);

        String text = getText.apply(item);

        holder.itemText.setText(text);
    }

    @Override
    public int getItemCount()
    {
        return getList.get().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView itemText;

        ViewHolder(View itemView)
        {
            super(itemView);
            itemText = itemView.findViewById(R.id.nameTextView);
        }
    }
}
