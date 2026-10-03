package stoneframe.serena.gui.tasks;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import stoneframe.serena.R;
import stoneframe.serena.tasks.TrustedSmsSender;

public class TrustedSmsSenderAdapter extends RecyclerView.Adapter<TrustedSmsSenderAdapter.ViewHolder>
{
    private final List<TrustedSmsSender> senders;
    private final Consumer<TrustedSmsSender> onRemove;

    public TrustedSmsSenderAdapter(List<TrustedSmsSender> senders, Consumer<TrustedSmsSender> onRemove)
    {
        this.senders = senders;
        this.onRemove = onRemove;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View row = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_item_trusted_sms_sender, parent, false);

        return new ViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        TrustedSmsSender sender = senders.get(position);

        holder.senderTextView.setText(sender.getContactName());
        holder.phoneNumberTextView.setText(sender.getPhoneNumber());
        holder.removeButton.setContentDescription(holder.itemView.getContext().getString(
            R.string.task_sms_remove_sender_description,
            sender.getContactName()));
        holder.removeButton.setOnClickListener(v -> onRemove.accept(sender));
    }

    @Override
    public int getItemCount()
    {
        return senders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder
    {
        final TextView senderTextView;
        final TextView phoneNumberTextView;
        final ImageButton removeButton;

        ViewHolder(View itemView)
        {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.trustedSmsSenderTextView);
            phoneNumberTextView = itemView.findViewById(R.id.trustedSmsSenderPhoneNumberTextView);
            removeButton = itemView.findViewById(R.id.removeTrustedSmsSenderButton);
        }
    }
}
