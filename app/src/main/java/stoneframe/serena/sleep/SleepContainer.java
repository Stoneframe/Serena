package stoneframe.serena.sleep;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

public class SleepContainer
{
    Sleep sleep;

    public SleepContainer()
    {
        sleep = new Sleep(LocalDate.now().toLocalDateTime(LocalTime.MIDNIGHT));
    }
}
