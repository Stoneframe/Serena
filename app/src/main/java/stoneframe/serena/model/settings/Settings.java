package stoneframe.serena.model.settings;

import org.joda.time.LocalTime;

import java.util.function.Supplier;

import stoneframe.serena.model.timeservices.TimeService;

public class Settings
{
    private final Supplier<SettingsContainer> container;

    private final TimeService timeService;

    public Settings(Supplier<SettingsContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public boolean isSilenceTime()
    {
        if (!isSilenceModeEnabled() || getSilenceStartTime().equals(getSilenceStopTime()))
        {
            return false;
        }

        return isWithinSilenceTimeInterval();
    }

    public boolean isSilenceModeEnabled()
    {
        return container.get().isSilenceModeEnabled;
    }

    public void setSilenceModeEnabled(boolean isEnabled)
    {
        container.get().isSilenceModeEnabled = isEnabled;
    }

    public LocalTime getSilenceStartTime()
    {
        return container.get().silenceStartTime;
    }

    public void setSilenceStartTime(LocalTime startTime)
    {
        container.get().silenceStartTime = startTime;
    }

    public LocalTime getSilenceStopTime()
    {
        return container.get().silenceStopTime;
    }

    public void setSilenceStopTime(LocalTime stopTime)
    {
        container.get().silenceStopTime = stopTime;
    }

    private boolean isWithinSilenceTimeInterval()
    {
        LocalTime currentTime = timeService.getNow().toLocalTime();

        if (getSilenceStartTime().isBefore(getSilenceStopTime()))
        {
            return !currentTime.isBefore(getSilenceStartTime())
                && !currentTime.isAfter(getSilenceStopTime());
        }
        else
        {
            return !(currentTime.isAfter(getSilenceStopTime())
                && currentTime.isBefore(getSilenceStartTime()));
        }
    }
}
