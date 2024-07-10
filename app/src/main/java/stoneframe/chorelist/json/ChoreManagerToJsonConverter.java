package stoneframe.chorelist.json;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import stoneframe.chorelist.model.chores.ChoreManager;
import stoneframe.chorelist.model.chores.ChoreSelector;
import stoneframe.chorelist.model.chores.EffortTracker;

public class ChoreManagerToJsonConverter
{
    public static String convertToJson(ChoreManager choreManager)
    {
        GsonBuilder builder = new GsonBuilder();

        Converters.registerLocalDate(builder);
        Converters.registerLocalTime(builder);
        Converters.registerLocalDateTime(builder);

        Gson gson = builder.create();

        return gson.toJson(choreManager);
    }

    public static ChoreManager convertFromJson(
        String json,
        EffortTrackerConverter effortTrackerConverter,
        ChoreSelectorConverter choreSelectorConverter)
    {
        GsonBuilder builder = new GsonBuilder();

        Converters.registerLocalDate(builder);
        Converters.registerLocalTime(builder);
        Converters.registerLocalDateTime(builder);

        builder.registerTypeAdapter(ChoreSelector.class, choreSelectorConverter);
        builder.registerTypeAdapter(EffortTracker.class, effortTrackerConverter);

        Gson gson = builder.create();

        return gson.fromJson(json, ChoreManager.class);
    }
}
