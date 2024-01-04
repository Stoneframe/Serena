package stoneframe.chorelist.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class UniversalConverter<FROM, TO> implements JsonDeserializer<FROM>
{
    private final Class<TO> clazz;

    public UniversalConverter(Class<TO> clazz)
    {
        this.clazz = clazz;
    }

    @Override
    public FROM deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException
    {
        return context.deserialize(json, clazz);
    }
}
