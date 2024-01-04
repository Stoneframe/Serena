package stoneframe.chorelist.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.SimpleEffortTracker;

public class SimpleEffortTrackerConverter implements EffortTrackerConverter
{
    @Override
    public EffortTracker deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context)
        throws JsonParseException
    {
        return context.deserialize(json, SimpleEffortTracker.class);
    }
}
