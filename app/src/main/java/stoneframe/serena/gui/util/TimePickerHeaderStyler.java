package stoneframe.serena.gui.util;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;

public final class TimePickerHeaderStyler
{
    private static final float HEADER_TEXT_SIZE_SP = 48f;
    private static final int HEADER_HEIGHT_DP = 72;

    private TimePickerHeaderStyler()
    {
    }

    public static void balanceHeaderValues(TimePicker timePicker)
    {
        resizeHeader(timePicker);
        styleHeaderValue(timePicker, "hours");
        styleHeaderValue(timePicker, "minutes");
        styleHeaderValue(timePicker, "separator");
    }

    private static void resizeHeader(TimePicker timePicker)
    {
        View headerView = findAndroidView(timePicker, "time_header");

        if (headerView == null) return;

        ViewGroup.LayoutParams layoutParams = headerView.getLayoutParams();

        if (layoutParams == null) return;

        float density = timePicker.getResources().getDisplayMetrics().density;
        layoutParams.height = Math.round(HEADER_HEIGHT_DP * density);
        headerView.setLayoutParams(layoutParams);
        headerView.requestLayout();
    }

    private static void styleHeaderValue(TimePicker timePicker, String resourceName)
    {
        View valueView = findAndroidView(timePicker, resourceName);

        if (!(valueView instanceof TextView)) return;

        TextView valueTextView = (TextView)valueView;
        valueTextView.setIncludeFontPadding(false);
        valueTextView.setMinWidth(0);
        valueTextView.setMinHeight(0);
        valueTextView.setPadding(0, 0, 0, 0);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, HEADER_TEXT_SIZE_SP);
    }

    private static View findAndroidView(TimePicker timePicker, String resourceName)
    {
        int resourceId = timePicker.getResources().getIdentifier(
            resourceName,
            "id",
            "android");

        return resourceId == 0 ? null : timePicker.findViewById(resourceId);
    }
}
