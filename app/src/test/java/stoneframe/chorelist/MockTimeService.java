package stoneframe.chorelist;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

import stoneframe.chorelist.model.TimeService;

public class MockTimeService implements TimeService
{
    @NonNull
    private DateTime now;

    public MockTimeService(@NonNull DateTime now)
    {
        this.now = now;
    }

    @NonNull
    @Override
    public DateTime getNow()
    {
        return now;
    }

    public void setNow(@NonNull DateTime now)
    {
        this.now = now;
    }
}
