package stoneframe.serena.gui.util;

import org.joda.time.LocalDateTime;

public class DateUtils
{
    public static LocalDateTime min(LocalDateTime a, LocalDateTime b)
    {
        if (a == null) return b;
        if (b == null) return a;
        return a.isBefore(b) ? a : b;
    }
}
