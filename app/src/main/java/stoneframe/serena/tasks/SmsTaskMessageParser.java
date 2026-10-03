package stoneframe.serena.tasks;

import androidx.annotation.Nullable;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public final class SmsTaskMessageParser
{
    private static final String HASHTAG = "#Serena";
    private static final int MAX_RELEVANT_LINES = 3;

    private SmsTaskMessageParser()
    {
    }

    public static @Nullable ParsedTask parse(@Nullable String messageBody)
    {
        if (messageBody == null)
        {
            return null;
        }

        List<String> lines = getFirstNonEmptyLines(messageBody);

        if (lines.size() < 2 || !HASHTAG.equals(lines.get(0)))
        {
            return null;
        }

        String description = getDescription(lines);
        LocalDate deadline = getDeadline(lines);

        boolean deadlineUnreadable = lines.size() == MAX_RELEVANT_LINES && deadline == null;

        return new ParsedTask(description, deadline, deadlineUnreadable);
    }

    private static List<String> getFirstNonEmptyLines(String messageBody)
    {
        List<String> lines = new ArrayList<>();

        for (String rawLine : messageBody.split("\\r\\n|\\r|\\n"))
        {
            String line = rawLine.trim();

            if (line.isEmpty())
            {
                continue;
            }

            lines.add(line);

            if (lines.size() == MAX_RELEVANT_LINES)
            {
                break;
            }
        }

        return lines;
    }

    private static String getDescription(List<String> lines)
    {
        return lines.get(1);
    }

    @Nullable
    private static LocalDate getDeadline(List<String> lines)
    {
        if (lines.size() < MAX_RELEVANT_LINES)
        {
            return null;
        }

        return parseDeadline(lines.get(2));
    }

    private static @Nullable LocalDate parseDeadline(String value)
    {
        try
        {
            return LocalDate.parse(value);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    public static final class ParsedTask
    {
        private final String description;
        private final @Nullable LocalDate deadline;
        private final boolean deadlineUnreadable;

        private ParsedTask(String description, @Nullable LocalDate deadline, boolean deadlineUnreadable)
        {
            this.description = description;
            this.deadline = deadline;
            this.deadlineUnreadable = deadlineUnreadable;
        }

        public String getDescription()
        {
            return description;
        }

        public @Nullable LocalDate getDeadline()
        {
            return deadline;
        }

        public boolean isDeadlineUnreadable()
        {
            return deadlineUnreadable;
        }
    }
}
