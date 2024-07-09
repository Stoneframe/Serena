package stoneframe.chorelist;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.TimeService;

public class MockTimeService implements TimeService
{
    @NonNull
    private LocalDateTime now;

    public MockTimeService(@NonNull LocalDateTime now)
    {
        this.now = now;
    }

    @NonNull
    @Override
    public LocalDateTime getNow()
    {
        return now;
    }

    @Override
    public LocalDate getToday()
    {
        return now.toLocalDate();
    }

    public void setNow(@NonNull LocalDateTime now)
    {
        this.now = now;
    }
}
