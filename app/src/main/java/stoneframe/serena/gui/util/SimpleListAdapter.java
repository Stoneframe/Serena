package stoneframe.serena.gui.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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
    private final boolean showItemCard;
    private final @DimenRes int minimumItemHeight;

    SimpleListAdapter(
        @NonNull Context context,
        @NonNull Supplier<List<T>> listFunction,
        @NonNull Function<T, String> mainTextFunction,
        @Nullable Function<T, String> secondaryTextFunction,
        @Nullable Function<T, String> bottomTextFunction,
        @Nullable Function<T, Integer> backgroundColorFunction,
        @Nullable Function<T, Integer> borderColorFunction,
        boolean showItemCard,
        @DimenRes int minimumItemHeight)
    {
        this.context = context;
        this.listFunction = listFunction;
        this.mainTextFunction = mainTextFunction;
        this.secondaryTextFunction = getOrDefault(secondaryTextFunction, x -> "");
        this.bottomTextFunction = getOrDefault(bottomTextFunction, x -> "");
        this.backgroundColorFunction = getOrDefault(
            backgroundColorFunction,
            x -> ContextCompat.getColor(context, R.color.surface));
        this.borderColorFunction = getOrDefault(
            borderColorFunction,
            x -> ContextCompat.getColor(context, R.color.outline));
        this.showItemCard = showItemCard;
        this.minimumItemHeight = minimumItemHeight;
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

        if (showItemCard)
        {
            GradientDrawable borderDrawable = new GradientDrawable();
            borderDrawable.setShape(GradientDrawable.RECTANGLE);
            float density = context.getResources().getDisplayMetrics().density;
            borderDrawable.setCornerRadius(12 * density);
            borderDrawable.setColor(backgroundColorFunction.apply(item));
            borderDrawable.setStroke(Math.max(1, Math.round(density)), borderColorFunction.apply(item));

            convertView.setBackground(borderDrawable);
        }
        else
        {
            convertView.setBackgroundColor(Color.TRANSPARENT);
            convertView.setMinimumHeight(
                context.getResources().getDimensionPixelSize(minimumItemHeight));

            if (secondaryTextView.getText().length() == 0 && bottomText.isEmpty())
            {
                convertView.setPadding(0, 0, 0, 0);

                RelativeLayout.LayoutParams mainTextLayoutParams =
                    (RelativeLayout.LayoutParams)mainTextView.getLayoutParams();
                mainTextLayoutParams.removeRule(RelativeLayout.ALIGN_BASELINE);
                mainTextLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                mainTextView.setLayoutParams(mainTextLayoutParams);
                mainTextView.setEllipsize(TextUtils.TruncateAt.END);
                mainTextView.setMaxLines(1);
            }
        }

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

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View dropDownView = getView(position, convertView, parent);

        if (!showItemCard)
        {
            int horizontalPadding = context.getResources()
                .getDimensionPixelSize(R.dimen.space_sm);
            dropDownView.setPaddingRelative(
                horizontalPadding,
                dropDownView.getPaddingTop(),
                horizontalPadding,
                dropDownView.getPaddingBottom());
        }

        return dropDownView;
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
