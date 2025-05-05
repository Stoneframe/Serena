package stoneframe.serena.sleep;

import org.joda.time.LocalDateTime;

public class SleepContainer
{
    Sleep sleep;

    public SleepContainer()
    {
        sleep = new Sleep(LocalDateTime.now());
    }
}
