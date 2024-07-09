package stoneframe.chorelist.model.timeservices;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.TimeService;

public class RealTimeService implements TimeService
{
    @Override
    public LocalDateTime getNow()
    {
        return LocalDateTime.now();
    }

    @Override
    public LocalDate getToday()
    {
        return LocalDate.now();
    }
}
