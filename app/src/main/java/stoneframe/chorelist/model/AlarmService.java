package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public interface AlarmService
{
    void setAlarm(DateTime dateTime, AlarmListener alarmListener);

    void cancelAlarm();
}
