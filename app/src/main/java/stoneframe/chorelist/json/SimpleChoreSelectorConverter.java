package stoneframe.chorelist.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.ChoreSelector;

public class SimpleChoreSelectorConverter implements ChoreSelectorConverter
{
    @Override
    public ChoreSelector deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context)
        throws JsonParseException
    {
        return context.deserialize(json, SimpleChoreSelector.class);
    }
}
