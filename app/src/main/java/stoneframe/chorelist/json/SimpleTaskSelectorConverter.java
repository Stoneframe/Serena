package stoneframe.chorelist.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.TaskSelector;

public class SimpleTaskSelectorConverter implements TaskSelectorConverter
{

    @Override
    public TaskSelector deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context)
        throws JsonParseException
    {
        return context.deserialize(json, SimpleTaskSelector.class);
    }

}
