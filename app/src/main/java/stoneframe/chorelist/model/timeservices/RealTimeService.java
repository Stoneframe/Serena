package stoneframe.chorelist.model.timeservices;

import org.joda.time.DateTime;

import stoneframe.chorelist.model.TimeService;

public class RealTimeService implements TimeService
{
    @Override
    public DateTime getNow()
    {
        return DateTime.now();
    }
}
