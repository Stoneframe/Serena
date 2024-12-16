package stoneframe.serena.model.settings;

import org.joda.time.LocalTime;

public class SettingsContainer
{
    boolean isSilenceModeEnabled;
    LocalTime silenceStartTime;
    LocalTime silenceStopTime;

    public SettingsContainer()
    {
        isSilenceModeEnabled = false;
        silenceStartTime = LocalTime.MIDNIGHT;
        silenceStopTime = LocalTime.MIDNIGHT;
    }
}
