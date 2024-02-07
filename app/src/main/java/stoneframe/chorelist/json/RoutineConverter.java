package stoneframe.chorelist.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import stoneframe.chorelist.model.DayRoutine;
import stoneframe.chorelist.model.FortnightRoutine;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.WeekRoutine;

public class RoutineConverter implements JsonDeserializer<Routine>
{
    @Override
    public Routine deserialize(
        JsonElement json,
        Type typeOfT,
        JsonDeserializationContext context) throws JsonParseException
    {
        switch (getRoutineType(json))
        {
            case Routine.DAY_ROUTINE:
                return context.deserialize(json, DayRoutine.class);
            case Routine.WEEK_ROUTINE:
                return context.deserialize(json, WeekRoutine.class);
            case Routine.FORTNIGHT_ROUTINE:
                return context.deserialize(json, FortnightRoutine.class);
            default:
                return null;
        }
    }

    private static int getRoutineType(JsonElement json)
    {
        JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jsonType = jsonObject.get("routineType");

        return jsonType.getAsInt();
    }
}
