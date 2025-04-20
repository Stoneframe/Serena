package stoneframe.serena.storages.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import stoneframe.serena.chores.EffortTracker;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;

public class WeeklyEffortTrackerConverter implements EffortTrackerConverter
{
    @Override
    public EffortTracker deserialize(
        JsonElement json, Type typeOfT,
        JsonDeserializationContext context)
        throws JsonParseException
    {
        return context.deserialize(json, WeeklyEffortTracker.class);
    }
}
